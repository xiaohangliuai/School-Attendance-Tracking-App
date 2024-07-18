package com.example.showattendance;

public class ClassItem {
    String className;
    int CRNs;

    public ClassItem(String className, int CRNs) {
        this.className = className;
        this.CRNs = CRNs;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setCRNs(int CRNs) {
        this.CRNs = CRNs;
    }

    public String getClassName() {
        return className;
    }

    public int getCRNs() {
        return CRNs;
    }
}
