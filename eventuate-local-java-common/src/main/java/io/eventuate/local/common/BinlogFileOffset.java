package io.eventuate.local.common;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class BinlogFileOffset {
  private String binlogFilename;
  private Long offset;

  public BinlogFileOffset() {
  }

  public BinlogFileOffset(String binlogFilename, Long offset) {
   this.binlogFilename = binlogFilename;
   this.offset = offset;
  }

  public BinlogFileOffset(Long offset) {
    this(null, offset);
  }

  public String getBinlogFilename() {
    return binlogFilename;
  }

  public void setBinlogFilename(String binlogFilename) {
    this.binlogFilename = binlogFilename;
  }

  public Long getOffset() {
    return offset;
  }

  public void setOffset(Long offset) {
    this.offset = offset;
  }

  public boolean isSameOrAfter(BinlogFileOffset binlogFileOffset) {

    if(this.equals(binlogFileOffset))
      return true;

    if(this.getBinlogFilename() == null || binlogFileOffset.getBinlogFilename() == null || this.getBinlogFilename().equals(binlogFileOffset.getBinlogFilename())) {
      if (binlogFileOffset.getOffset() == null)
        return true;
      else if (this.getOffset() == null)
        return true;
      else if(this.getOffset() > binlogFileOffset.getOffset()) {
        return true;
      }
    } else {
      if(this.getBinlogFilename().compareTo(binlogFileOffset.getBinlogFilename()) > 0) {
        return true;
      }
    }

    return false;
  }

  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
