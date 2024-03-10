package js.app;

import static js.base.Tools.*;

import java.util.List;

import js.base.Pair;

public class HelpFormatter {

  public void addItem(String item, Object details) {
    checkState(mString == null);
    mItemList.add(pair(item, details));
  }

  public String toString() {
    if (mString == null)
      mString = buildString();
    return mString;
  }

  private String buildString() {
    var sb = new StringBuilder();

    int itemsWidth = 18;
    int sep = 2;

    for (var item : mItemList) {
      var label = item.first;
      var details = item.second.toString().stripTrailing();

      if (label.length() > itemsWidth) {
        todo("support splitting at space");
      }
      var i = sb.length();
      sb.append(label);
      var sp = i + itemsWidth + sep - sb.length();
      if (sp <= 0) {
        sb.append('\n');
        sp = itemsWidth + sep;
      }
      sb.append(spaces(sp));

      var lines = split(details, '\n');
      boolean needIndent = false;
      for (var x : lines) {
        if (needIndent)
          sb.append(spaces(itemsWidth + sep));
        sb.append(x);
        sb.append('\n');
        needIndent = true;
      }
    }
    return sb.toString();
  }

  private List<Pair<String, Object>> mItemList = arrayList();
  private String mString;
}
