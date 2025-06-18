package com.pipemasters.server.entity.enums;

public enum AbsenceCause {
    DEVICE_FAILURE,     // неисправность регистратора
    REGULATORY_EXEMPT,  // не требовалась запись
    HUMAN_FACTOR,       // забыли/не выгрузили
    OTHER
}
