package com.example.backend2.global.annotation

import com.example.backend2.data.Role

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(
    AnnotationRetention.RUNTIME
)
@MustBeDocumented
annotation class HasRole(
    val value: Role // 권한을 파라미터로 받음
)