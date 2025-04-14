package js.app.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class ProjectInfo implements AbstractData {

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    return m;
  }

  @Override
  public ProjectInfo build() {
    return this;
  }

  @Override
  public ProjectInfo parse(Object obj) {
    return new ProjectInfo((JSMap) obj);
  }

  private ProjectInfo(JSMap m) {
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof ProjectInfo))
      return false;
    ProjectInfo other = (ProjectInfo) object;
    if (other.hashCode() != hashCode())
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      m__hashcode = r;
    }
    return r;
  }

  protected int m__hashcode;

  public static final class Builder extends ProjectInfo {

    private Builder(ProjectInfo m) {

    }

    @Override
    public Builder toBuilder() {
      return this;
    }

    @Override
    public int hashCode() {
      m__hashcode = 0;
      return super.hashCode();
    }

    @Override
    public ProjectInfo build() {
      ProjectInfo r = new ProjectInfo();

      return r;
    }

  }

  public static final ProjectInfo DEFAULT_INSTANCE = new ProjectInfo();

  private ProjectInfo() {
  }

}
