package js.app;

import static js.base.Tools.*;

import js.app.gen.ProjectInfo;
import js.base.BaseObject;
import js.file.DirWalk;
import js.file.Files;

import java.io.File;
import java.util.List;

public class ProjectOrganizer extends BaseObject {

  private static final String EXT = Files.EXT_JPROJ;
  private static final String DOTTED_EXT = "." + EXT;

  public static final ProjectOrganizer SHARED_INSTANCE = new ProjectOrganizer();

  private ProjectOrganizer() {
    alertVerbose();
  }

  public void addProjectHint(File dir) {
    var d = Files.absolute(dir);
    log("...adding hint:", d);
    if (!mHints.contains(d))
      mHints.add(d);
  }

  private List<File> mHints = arrayList();

  public boolean defined() {
    return mProjectDirectory != null;
  }

  /**
   * Attempt to find a project file (xxxx.jproj) in a directory heirarchy.
   * Starts with a file (or directory) and searches upward in the heirarchy until it finds a .jproj file.
   * If it doesn't find one, and mustFind is false, it uses the first directory encountered in the search;
   * whereas if mustFind is true, it throws an exception
   */
  public boolean defineProject(boolean mustFind) {
    log("defineProject");
    if (mHints.isEmpty()) {
      addProjectHint(Files.currentDirectory());

    }
//    auxDefine(fileOrDirectoryToStartSearch);
    if (!defined()) {
      for (var hint : mHints) {
//        if (hint.equals(fileOrDirectoryToStartSearch)) {
//          continue;
//        }
        auxDefine(hint);
        if (defined()) break;
      }
    }
    if (mustFind && !defined()) {
      badState("Can't find", DOTTED_EXT, "in any of:", mHints);
    }
    return defined();
  }

  public void createProject(File optDirectory, String projectName) {
    if (Files.empty(optDirectory)) {
      optDirectory = Files.currentDirectory();
    }
    var f = new File(optDirectory, projectName + DOTTED_EXT);
    if (f.exists()) return;
    Files.S.write(f, ProjectInfo.DEFAULT_INSTANCE);
    mProjectDirectory = optDirectory;
  }

  private void auxDefine(File fileOrDirectoryToStartSearch) {
    log("...searching within:", fileOrDirectoryToStartSearch);
    File dir = null;
    var c = fileOrDirectoryToStartSearch;
    Files.assertNonEmpty(c);
    if (c.isDirectory()) {
      dir = c;
    } else if (c.isFile()) {
      dir = Files.parent(c);
    } else {
      while (true) {
        c = c.getParentFile();
        if (c == null) break;
        if (c.isDirectory()) {
          dir = c;
          break;
        }
      }
    }
    while (dir != null) {
      var dw = new DirWalk(dir).withRecurse(false).withExtensions(EXT);
      var fls = dw.files();
      if (!fls.isEmpty()) {
        mProjectDirectory = dir;
        log("...setting project directory to:", mProjectDirectory);
        break;
      }
      dir = dir.getParentFile();
    }
  }

  public File optProjectDirectory() {
    return mProjectDirectory;
  }

  public File projectDirectory() {
    var dir = optProjectDirectory();
    return Files.assertNonEmpty(dir, "project directory");
  }

  public File absoluteWithinProject(File relFile) {
    Files.assertNonEmpty(relFile);
    if (relFile.isAbsolute()) {
      alert("absoluteWithinProject given abs path: " + relFile);
      relFile = relativeToProject(relFile);
    }
    return new File(projectDirectory(), relFile.getPath());
  }

  public File relativeToProject(File absFile) {
    if (verbose())
      log("relativeToProject:", Files.infoMap(absFile));
    Files.assertAbsolute(absFile);
    var f = Files.relativeToContainingDirectory(absFile, projectDirectory());
    log("=>", f);
    return f;
  }

  public File optRelToAbs(String strOrNull) {
    if (nullOrEmpty(strOrNull)) {
      return Files.DEFAULT;
    }
    var f = new File(strOrNull);
    return absoluteWithinProject(f);
  }

  private File mProjectDirectory;
}
