package com.auranite.abloom;

import java.lang.annotation.*;

/**
 * Аннотация для указания приоритета обработки события урона.
 * Позволяет контролировать порядок обработки урона другими модами.
 * 
 * <p>Приоритеты обрабатываются в порядке убывания (большее число = выше приоритет).</p>
 * 
 * <p>По умолчанию используется приоритет 0. Моды могут использовать
 * отрицательные значения для обработки после Abloom или положительные
 * для обработки до Abloom.</p>
 * 
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
@Documented
public @interface DamagePriority {
    /**
     * Значение приоритета обработки события урона.
     * @return значение приоритета (по умолчанию 0)
     */
    int value() default 0;
}
