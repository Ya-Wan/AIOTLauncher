package com.android.launcher3.aiot;

import java.io.Serializable;

public class AodAoitEnum implements Serializable
{
    private static final long serialVersionUID = 1L;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    private String name;
    private String value;

    AodAoitEnum(String name, String value)
    {
        this.name = name;
        this.value = value;
    }
}
