package com.teaching.system.domain;

/**
 * 意见反馈文件类
 * 
 * @author teaching
 */
public class SuggBackFile
{
    /** 文件名称 */
    private String name;

    /** 文件URL */
    private String url;

    public SuggBackFile()
    {
    }

    public SuggBackFile(String name, String url)
    {
        this.name = name;
        this.url = url;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    @Override
    public String toString()
    {
        return "SuggBackFile{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
