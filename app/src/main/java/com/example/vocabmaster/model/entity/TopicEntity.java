package com.example.vocabmaster.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.vocabmaster.R;

@Entity(tableName = "topics")
public class TopicEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "topic_id")
    private int id;

    @ColumnInfo(name = "topic_name")
    private String title;

    private String subTitle;
    private int learnedCount;
    private int totalCount;
    private int iconResId;

    public TopicEntity(String title, String subTitle, int learnedCount, int totalCount, int iconResId) {
        this.title = title;
        this.subTitle = subTitle;
        this.learnedCount = learnedCount;
        this.totalCount = totalCount;
        this.iconResId = iconResId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubTitle() { return subTitle; }
    public void setSubTitle(String subTitle) { this.subTitle = subTitle; }

    public int getLearnedCount() { return learnedCount; }
    public void setLearnedCount(int learnedCount) { this.learnedCount = learnedCount; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public int getIconResId() {
        if (iconResId == 0 && title != null) {
            switch (title.trim()) {
                case "Gia đình":
                    return R.drawable.ic_family;
                case "Trường học":
                    return R.drawable.ic_school;
                case "Công việc":
                    return R.drawable.ic_congviec;
                case "Đồ ăn":
                    return R.drawable.ic_doan;
                case "Du lịch":
                    return R.drawable.ic_dulich;
                case "Công nghệ":
                    return R.drawable.ic_tech;
                default:
                    return R.drawable.ic_home;
            }
        }
        return iconResId != 0 ? iconResId : R.drawable.ic_home;
    }

    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
}
