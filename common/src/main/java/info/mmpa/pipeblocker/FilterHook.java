package info.mmpa.pipeblocker;

public interface FilterHook {
    CheckStatus check(Class<?> underlyingClass, FilterMatchType matchType, CheckStatus status);
}
