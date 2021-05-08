Lambda表达式总结

```
tagsCreateCmd.getTageOptionValueList().stream().filter(
        e-> !valueSet.contains(e.getTagsValueKey()) 
);
```

```
tagsCreateCmd.getTageOptionValueList().stream().filter(
        (e) ->
        {
            return !valueSet.contains(e.getTagsValueKey());
        }
);
```

两种方式一样