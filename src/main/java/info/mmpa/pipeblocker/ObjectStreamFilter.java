package info.mmpa.pipeblocker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.misc.ObjectInputFilter;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ObjectStreamFilter {
    private static final Logger LOGGER = LogManager.getLogger("PipeBlocker");

    private static final List<Pattern> allowedPatterns = new ArrayList<>();
    private static final List<Pattern> rejectedPatterns = new ArrayList<>();

    private static final Set<Class<?>> REJECTED_CLASSES = Collections.synchronizedSet(new HashSet<>());

    private static void loadFilter() {
        // Attempt to load filter from GitHub
        try(InputStream filterStream = new URL("https://raw.githubusercontent.com/Minecraft-Malware-Prevention-Alliance/PipeBlocker/main/src/main/resources/pipeblocker_filter.txt").openStream()) {
            processFilter(filterStream);
        } catch(IOException e) {
            LOGGER.warn("Failed to load online filter, using local version", e);
            try(InputStream localFilterStream = ObjectStreamFilter.class.getResourceAsStream("/pipeblocker_filter.txt")) {
                if(localFilterStream == null)
                    throw new FileNotFoundException("pipeblocker_filter.txt");
                processFilter(localFilterStream);
            } catch(IOException e2) {
                LOGGER.error("Failed to load local filter, this will mean no deserialization is permitted", e2);
            }
        }
    }

    private static void processFilter(InputStream filterStream) throws IOException {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(filterStream))) {
            String line = reader.readLine();
            while(line != null) {
                processLine(line.trim());
                line = reader.readLine();
            }
        }
        LOGGER.info("Successfully loaded PipeBlocker filter.");
    }

    private static void processLine(String line) {
        // ignore blank and comments
        if(line.length() == 0 || line.charAt(0) == '#')
            return;
        // process glob lines
        if(line.charAt(0) == '+' || line.charAt(0) == '-') {
            boolean isAllowing = line.charAt(0) == '+';
            String glob = line.substring(1);
            LOGGER.debug("Adding {} rule for glob '{}'", isAllowing ? "allow" : "deny", glob);
            Pattern desiredPattern = Pattern.compile(convertGlobToRegex(glob));
            if(isAllowing)
                allowedPatterns.add(desiredPattern);
            else
                rejectedPatterns.add(desiredPattern);
        }
    }

    private static Stream<Class<?>> inheritanceStream(Class<?> clz) {
        if(clz == null)
            return Stream.empty();
        Stream.Builder<Class<?>> streamBuilder = Stream.builder();
        while(clz != null) {
            streamBuilder.add(clz);
            clz = clz.getSuperclass();
        }
        return streamBuilder.build();
    }

    private static boolean isRejectedName(String name) {
        for(Pattern p : rejectedPatterns) {
            if(p.matcher(name).matches())
                return true;
        }
        return false;
    }

    private static boolean isAllowedName(String name) {
        for(Pattern p : allowedPatterns) {
            if(p.matcher(name).matches())
                return true;
        }
        return false;
    }

    public static void apply() {
        loadFilter();
        ObjectInputFilter.Config.setSerialFilter(new ObjectInputFilter() {
            @Override
            public Status checkInput(FilterInfo filterInfo) {
                if(filterInfo.serialClass() == null)
                    return Status.UNDECIDED;

                // If any of the classes are explicitly denied, deny
                if(inheritanceStream(filterInfo.serialClass()).map(Class::getCanonicalName).anyMatch(ObjectStreamFilter::isRejectedName)) {
                    if(REJECTED_CLASSES.add(filterInfo.serialClass())) {
                        LOGGER.warn("Blocked class {} from being deserialized as it's not allowed", filterInfo.serialClass().getName());
                    }
                    return Status.REJECTED;
                }

                // If any of the classes are explicitly allowed, allow
                if(inheritanceStream(filterInfo.serialClass()).map(Class::getCanonicalName).anyMatch(ObjectStreamFilter::isAllowedName)) {
                    return Status.UNDECIDED;
                }

                return Status.REJECTED;
            }
        });
    }

    /**
     * Converts a standard POSIX Shell globbing pattern into a regular expression
     * pattern. The result can be used with the standard {@link java.util.regex} API to
     * recognize strings which match the glob pattern.
     * <p></p>
     * See also, the POSIX Shell language:
     * http://pubs.opengroup.org/onlinepubs/009695399/utilities/xcu_chap02.html#tag_02_13_01
     *
     * Code from https://stackoverflow.com/a/17369948, licensed under the public domain
     * @param pattern A glob pattern.
     * @return A regex pattern to recognize the given glob pattern.
     */
    public static String convertGlobToRegex(String pattern) {
        StringBuilder sb = new StringBuilder(pattern.length());
        int inGroup = 0;
        int inClass = 0;
        int firstIndexInClass = -1;
        char[] arr = pattern.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char ch = arr[i];
            switch (ch) {
                case '\\':
                    if (++i >= arr.length) {
                        sb.append('\\');
                    } else {
                        char next = arr[i];
                        switch (next) {
                            case ',':
                                // escape not needed
                                break;
                            case 'Q':
                            case 'E':
                                // extra escape needed
                                sb.append('\\');
                            default:
                                sb.append('\\');
                        }
                        sb.append(next);
                    }
                    break;
                case '*':
                    if (inClass == 0)
                        sb.append(".*");
                    else
                        sb.append('*');
                    break;
                case '?':
                    if (inClass == 0)
                        sb.append('.');
                    else
                        sb.append('?');
                    break;
                case '[':
                    inClass++;
                    firstIndexInClass = i+1;
                    sb.append('[');
                    break;
                case ']':
                    inClass--;
                    sb.append(']');
                    break;
                case '.':
                case '(':
                case ')':
                case '+':
                case '|':
                case '^':
                case '$':
                case '@':
                case '%':
                    if (inClass == 0 || (firstIndexInClass == i && ch == '^'))
                        sb.append('\\');
                    sb.append(ch);
                    break;
                case '!':
                    if (firstIndexInClass == i)
                        sb.append('^');
                    else
                        sb.append('!');
                    break;
                case '{':
                    inGroup++;
                    sb.append('(');
                    break;
                case '}':
                    inGroup--;
                    sb.append(')');
                    break;
                case ',':
                    if (inGroup > 0)
                        sb.append('|');
                    else
                        sb.append(',');
                    break;
                default:
                    sb.append(ch);
            }
        }
        return sb.toString();
    }
}
