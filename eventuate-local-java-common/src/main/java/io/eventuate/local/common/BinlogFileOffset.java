package io.eventuate.local.common;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Optional;

public class BinlogFileOffset {
  private String binlogFilename;
  private long offset;
  private int rowsToSkip;
  private Optional<Gtid> gtid;

  public BinlogFileOffset() {
    this("", 0);
  }

  public BinlogFileOffset(String binlogFilename, long offset) {
   this(binlogFilename, offset, 0);
  }

  public BinlogFileOffset(String binlogFilename, long offset, Gtid gtid) {
    this(binlogFilename, offset, 0, gtid);
  }

  public BinlogFileOffset(String binlogFilename, long offset, Optional<Gtid> gtid) {
    this(binlogFilename, offset, 0, gtid);
  }

  public BinlogFileOffset(String binlogFilename, long offset, int rowsToSkip) {
    this(binlogFilename, offset, rowsToSkip, Optional.empty());
  }

  public BinlogFileOffset(String binlogFilename, long offset, int rowsToSkip, Gtid gtid) {
    this(binlogFilename, offset, rowsToSkip, Optional.of(gtid));
  }

  public BinlogFileOffset(String binlogFilename, long offset, int rowsToSkip, Optional<Gtid> gtid) {
    this.binlogFilename = binlogFilename;
    this.offset = offset;
    this.rowsToSkip = rowsToSkip;
    this.gtid = gtid;
  }

  public String getBinlogFilename() {
    return binlogFilename;
  }

  public void setBinlogFilename(String binlogFilename) {
    this.binlogFilename = binlogFilename;
  }

  public long getOffset() {
    return offset;
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public int getRowsToSkip() {
    return rowsToSkip;
  }

  public void setRowsToSkip(int rowsToSkip) {
    this.rowsToSkip = rowsToSkip;
  }

  public Optional<Gtid> getGtid() {
    return gtid;
  }

  public void setGtid(Optional<Gtid> gtid) {
    this.gtid = gtid;
  }

  public boolean isSameOrAfter(BinlogFileOffset binlogFileOffset) {
    if(this.equals(binlogFileOffset))
      return true;

    if(this.getBinlogFilename().equals(binlogFileOffset.getBinlogFilename())) {
      if(this.getOffset()>binlogFileOffset.getOffset()) {
        return true;
      }
    } else {
      if(this.getBinlogFilename().compareTo(binlogFileOffset.getBinlogFilename())>0) {
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
