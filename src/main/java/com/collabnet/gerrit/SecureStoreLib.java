package com.collabnet.gerrit;

import com.google.gerrit.common.FileUtil;
import com.google.gerrit.server.config.SitePaths;
import com.google.gerrit.server.securestore.SecureStore;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jgit.internal.storage.file.LockFile;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Singleton
public class SecureStoreLib implements SecureStore {
  private static final String PREFIX = "test-";
  private final FileBasedConfig sec;

  @Inject
  SecureStoreLib(SitePaths site) {
    File secureConfig = new File(site.etc_dir, "secure.config");
    sec = new FileBasedConfig(secureConfig, FS.DETECTED);
    try {
      sec.load();
    } catch (Exception e) {
      throw new RuntimeException("Cannot load secure.config", e);
    }
  }

  @Override
  public String get(String section, String subsection, String name) {
    String value = sec.getString(section, subsection, name);
    if (value != null && value.startsWith(PREFIX)) {
      return value.substring(PREFIX.length());
    }
    return value;
  }

  @Override
  public String[] getList(String section, String subsection, String name) {
    return sec.getStringList(section, subsection, name);
  }

  @Override
  public void set(String section, String subsection, String name, String value) {
    if (value != null) {
      sec.setString(section, subsection, name, PREFIX + value);
    } else {
      sec.unset(section, subsection, name);
    }
    save();
  }

  @Override
  public void setList(String section, String subsection, String name,
      List<String> values) {
    if (values != null) {
      sec.setStringList(section, subsection, name, values);
    } else {
      sec.unset(section, subsection, name);
    }
    save();
  }

  @Override
  public void unset(String section, String subsection, String name) {
    sec.unset(section, subsection, name);
    save();
  }

  private void save() {
    try {
      saveSecure(sec);
    } catch (IOException e) {
      throw new RuntimeException("Cannot save secure.config", e);
    }
  }

  private static void saveSecure(final FileBasedConfig sec) throws IOException {
    if (FileUtil.modified(sec)) {
      final byte[] out = Constants.encode(sec.toText());
      final File path = sec.getFile();
      final LockFile lf = new LockFile(path, FS.DETECTED);
      if (!lf.lock()) {
        throw new IOException("Cannot lock " + path);
      }
      try {
        FileUtil.chmod(0600, new File(path.getParentFile(), path.getName()
            + ".lock"));
        lf.write(out);
        if (!lf.commit()) {
          throw new IOException("Cannot commit write to " + path);
        }
      } finally {
        lf.unlock();
      }
    }
  }
}
