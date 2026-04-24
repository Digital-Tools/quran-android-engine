// AGP and Kotlin are provided by buildSrc convention plugins.
// Only declare plugins NOT on the buildSrc classpath here.
plugins {
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
