package com.scrm.robot.taskmanager.enums;

public enum RobotRunState {
    STARTED("已启动",0),
    WAITING("等待中",1),
    STOPPED("已停止",2);

    public String name;
    public int value;

    private RobotRunState(String name, int value){
        this.name=name;
        this.value=value;
    }

}
