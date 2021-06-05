package com.card.script.diff.model


class DependenciesDiffFileModel implements Comparable<DependenciesDiffFileModel> {
    public static final int CHANGE_NEW = 1;//新文件
    public static final int CHANGE_HIGH = 2;//增大
    public static final int CHANGE_EQ = 3;//相等
    public static final int CHANGE_LOW = 4;//减小
    public static final int CHANGE_DEL = 5;//删除
    //文件状态
    public int fileChange = 0
    //第一个文件大小
    public long firstFileSize = 0
    //第二个文件大小
    public long secondFileSize = 0
    //差异
    public long diffFileSize = 0

    public int fileType = TYPE_UNKNOW
    public static final int TYPE_CLASS = 1
    public static final int TYPE_SO = 2
    public static final int TYPE_RES = 3
    public static final int TYPE_ASSETS = 4
    public static final int TYPE_UNKNOW = 5



    public String filePath
    public String fileShortPath
    public String fileName


    @Override
    int compareTo(DependenciesDiffFileModel o) {
        return o.diffFileSize - diffFileSize
    }

    def handle() {
        diffFileSize = firstFileSize - secondFileSize
        handleFileType()
    }

    def handleFileType() {
        if (fileName.contains(".class")) {
            fileType = TYPE_CLASS
        } else if (fileName.contains(".so")) {
            fileType = TYPE_SO
        } else if (filePath.contains("/res/")) {
            fileType = TYPE_RES
        } else if (filePath.contains("/assets/")) {
            fileType = TYPE_ASSETS
        } else {
            fileType = TYPE_UNKNOW
        }
    }

    static String getType(int type) {
        if (type == TYPE_CLASS) {
            return 'class'
        } else if (type == TYPE_SO) {
            return 'so'
        } else if (type == TYPE_RES) {
            return 'res'
        } else if (type == TYPE_ASSETS) {
            return 'assets'
        } else {
            return 'unknow'
        }
    }


    String getChange() {
        if (fileChange == CHANGE_NEW) {
            return "新文件"
        } else if (fileChange == CHANGE_HIGH) {
            return "增  大"
        } else if (fileChange == CHANGE_EQ) {
            return "相  等"
        } else if (fileChange == CHANGE_LOW) {
            return "减  小"
        } else if (fileChange == CHANGE_DEL) {
            return "删  除"
        }
    }
}
