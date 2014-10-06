This is a example implementation of Gerrit Secure Store extension point. It will add/remove 'test-' prefix to all stored/retrieved properties.

## Build

```
mvn package
```

## Deploy

Copy jar file from `target/` directory to Gerrit `$site_path/lib/` folder. Then add `gerrit.secureStoreClass = com.collabnet.gerrit.SecureStoreLib` property to `gerrit.config` file.
