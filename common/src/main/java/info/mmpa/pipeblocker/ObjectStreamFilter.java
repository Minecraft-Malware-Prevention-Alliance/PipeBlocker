package info.mmpa.pipeblocker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ObjectStreamFilter {
    private static final Logger LOGGER = LogManager.getLogger("PipeBlocker");

    private static final List<Pattern> allowedPatterns = new ArrayList<>();
    private static final List<Pattern> rejectedPatterns = new ArrayList<>();
    private static final List<Pattern> softAllowedPatterns = new ArrayList<>();

    private static final Set<Class<?>> REJECTED_CLASSES = Collections.synchronizedSet(new HashSet<>());

    private static int numEntriesLoaded = 0;

    private static void loadFilter() {
        // Attempt to load filter from GitHub
        try(InputStream filterStream = new URL("https://minecraft-malware-prevention-alliance.github.io/PipeBlocker/pipeblocker_filter.txt?t=" + new Date().getTime()).openStream()) {
            processFilter(filterStream);
            LOGGER.info("Successfully loaded online PipeBlocker filter with {} entries.", numEntriesLoaded);
        } catch(IOException e) {
            LOGGER.warn("Failed to load online filter, using local version", e);
            try(InputStream localFilterStream = ObjectStreamFilter.class.getResourceAsStream("/pipeblocker_filter.txt")) {
                if(localFilterStream == null)
                    throw new FileNotFoundException("pipeblocker_filter.txt");
                processFilter(localFilterStream);
                LOGGER.info("Successfully loaded local PipeBlocker filter with {} entries.", numEntriesLoaded);
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
    }

    private static void processLine(String line) {
        // ignore blank and comments
        if(line.length() == 0 || line.charAt(0) == '#')
            return;
        // process glob lines
        String type = null;
        List<Pattern> list = null;
        switch (line.charAt(0)) {
            case '+': {
                type = "allow";
                list = allowedPatterns;
                break;
            }
            case '-': {
                type = "deny";
                list = rejectedPatterns;
                break;
            }
            case '~': {
                type = "soft allow";
                list = softAllowedPatterns;
                break;
            }
        }
        if (list != null) {
            String glob = line.substring(1);
            LOGGER.debug("Adding {} rule for glob '{}'", new Object[] {type, glob});
            Pattern desiredPattern = Pattern.compile(convertGlobToRegex(glob));
            list.add(desiredPattern);
            numEntriesLoaded++;
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

    private static boolean isSoftAllowedName(String name) {
        for(Pattern p : softAllowedPatterns) {
            if(p.matcher(name).matches())
                return true;
        }
        return false;
    }

    public static CheckStatus check(Class<?> clazz) {
        if (clazz == null)
            return CheckStatus.UNDECIDED;

        Class<?> underlyingClass = clazz;
        while (underlyingClass.isArray()) {
            underlyingClass = underlyingClass.getComponentType();
        }

        // Validate that none of the classes are explicitly denied
        if (inheritanceStream(underlyingClass).map(Class::getCanonicalName).noneMatch(ObjectStreamFilter::isRejectedName)) {
            // If any of the classes are explicitly allowed, allow
            if (inheritanceStream(underlyingClass).map(Class::getCanonicalName).anyMatch(ObjectStreamFilter::isAllowedName)
                || isSoftAllowedName(underlyingClass.getCanonicalName())) {
                return CheckStatus.UNDECIDED;
            }
        }
        if (REJECTED_CLASSES.add(underlyingClass)) {
            LOGGER.warn("Blocked class {} from being deserialized as it's not allowed", underlyingClass.getName());
        }
        if (Objects.equals(System.getProperty("info.mmpa.pipeblocker.log-only"), "true")) {
            return CheckStatus.UNDECIDED;
        } else {
            return CheckStatus.REJECTED;
        }
    }


    public static void apply() {
        loadFilter();
        String javaVersion = System.getProperties().getProperty("java.specification.version");
        String className;
        if ("1.8".equals(javaVersion)) {
            className = "info.mmpa.pipeblocker.java8.FilterSetter";
        } else if (javaVersion.chars().allMatch(Character::isDigit) && Integer.parseInt(javaVersion) > 8) {
            className = "info.mmpa.pipeblocker.java9.FilterSetter";
        } else {
            System.err.println("Unsupported java version: " + javaVersion);
            throw new RuntimeException("Unsupported java version: " + javaVersion);
        }
        try {
            Class.forName(className).getMethod("apply").invoke(null);
        } catch (ReflectiveOperationException ex) {
            System.out.println("Unable to invoke setter");
            throw new RuntimeException(ex);
        }
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
        StringBuilder sb = new StringBuilder(pattern.length() + 2);
        sb.append('^');
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
        sb.append('$');
        return sb.toString();
    }
}
