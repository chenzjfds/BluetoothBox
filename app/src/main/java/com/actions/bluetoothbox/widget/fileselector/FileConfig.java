package com.actions.bluetoothbox.widget.fileselector;

import java.io.Serializable;

/**
 * 文件选择器的配置类
 */
public class FileConfig implements Serializable {

    public static final String FILE_CONFIG="fileConfig";

    public boolean showHiddenFiles = false;
    public int theme;
    public int filterModel;
    public String[] filter;
    public boolean positiveFiter;
    public String title;//选择框的标题
    public boolean easyMode;//是否开启简易模式
    public String rootPath;//文件选择可以访问的最高路径,默认为"/"
    public String startPath;//文件选择器的开始的路径，默认为"/"
    public boolean multiModel;//多选模式
    public int selectType;//选择文件的种类:文件夹、文件、全部
    public boolean actionBarMode;//在Activity中使用ActionBar来替换一部分操作

    public FileConfig() {

        theme = FileTheme.THEME_WHITE;
        filterModel = FileFilter.FILTER_NONE;
        positiveFiter = true;
        title = "选择文件";
        easyMode = false;
        rootPath="/";
        startPath="/";
        multiModel=false;
        selectType= FileType.All;
        actionBarMode=true;

    }

    private FileConfig(Builder builder) {
        this.showHiddenFiles = builder.showHiddenFiles;
        this.theme = builder.theme;
        this.filterModel = builder.filterModel;
        this.filter = builder.filter;
        this.positiveFiter = builder.positiveFiter;
        this.title = builder.title;
        this.easyMode = builder.easyMode;
        this.rootPath = builder.rootPath;
        this.startPath = builder.startPath;
        this.multiModel = builder.multiModel;
        this.selectType = builder.selectType;
        this.actionBarMode = builder.actionBarMode;
    }


    public static class Builder {
        private boolean showHiddenFiles = false;
        private int theme= FileTheme.THEME_WHITE;;
        private int filterModel  = FileFilter.FILTER_NONE;;
        private String[] filter;
        private boolean positiveFiter= true;
        private String title= "选择文件";
        private boolean easyMode= false;
        private String rootPath="/";
        private String startPath="/";
        private boolean multiModel=false;
        private int selectType= FileType.All;
        private boolean actionBarMode=true;;

        public Builder showHiddenFiles(boolean showHiddenFiles) {
            this.showHiddenFiles = showHiddenFiles;
            return this;
        }

        public Builder theme(int theme) {
            this.theme = theme;
            return this;
        }

        public Builder filterModel(int filterModel) {
            this.filterModel = filterModel;
            return this;
        }

        public Builder filter(String[] filter) {
            this.filter = filter;
            return this;
        }

        public Builder positiveFiter(boolean positiveFiter) {
            this.positiveFiter = positiveFiter;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder easyMode(boolean easyMode) {
            this.easyMode = easyMode;
            return this;
        }

        public Builder rootPath(String rootPath) {
            this.rootPath = rootPath;
            return this;
        }

        public Builder startPath(String startPath) {
            this.startPath = startPath;
            return this;
        }

        public Builder multiModel(boolean multiModel) {
            this.multiModel = multiModel;
            return this;
        }

        public Builder selectType(int selectType) {
            this.selectType = selectType;
            return this;
        }

        public Builder actionBarMode(boolean actionBarMode) {
            this.actionBarMode = actionBarMode;
            return this;
        }

        public FileConfig build() {
            return new FileConfig(this);
        }
    }
}
