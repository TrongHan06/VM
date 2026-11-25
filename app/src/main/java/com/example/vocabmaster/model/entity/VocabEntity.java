package com.example.vocabmaster.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "vocabularies",
        foreignKeys = @ForeignKey(entity = TopicEntity.class,
                                  parentColumns = "topic_id",
                                  childColumns = "topic_id",
                                  onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = {"topic_id"})})
public class VocabEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "vocabulary_id")
    private int id;

    @ColumnInfo(name = "topic_id")
    private int topicId;

    private String word;

    @ColumnInfo(name = "ipa")
    private String phonetic;

    private String meaning;

    @ColumnInfo(name = "example_en")
    private String example;

    @ColumnInfo(name = "example_vi")
    private String exampleMeaning;

    @ColumnInfo(name = "word_type")
    private String wordType;

    private String level;

    @ColumnInfo(name = "is_favorite")
    private boolean isFavorite;

    private boolean isLearned;

    public VocabEntity(int topicId, String word, String phonetic, String meaning, String example, String exampleMeaning, boolean isLearned, boolean isFavorite) {
        this.topicId = topicId;
        this.word = word;
        this.phonetic = phonetic;
        this.meaning = meaning;
        this.example = example;
        this.exampleMeaning = exampleMeaning;
        this.isLearned = isLearned;
        this.isFavorite = isFavorite;
        this.wordType = "n";
        this.level = "A1";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTopicId() { return topicId; }
    public void setTopicId(int topicId) { this.topicId = topicId; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }

    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }

    public String getExampleMeaning() { return exampleMeaning; }
    public void setExampleMeaning(String exampleMeaning) { this.exampleMeaning = exampleMeaning; }

    public String getWordType() { return wordType; }
    public void setWordType(String wordType) { this.wordType = wordType; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public boolean isLearned() { return isLearned; }
    public void setLearned(boolean learned) { isLearned = learned; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
