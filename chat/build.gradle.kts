// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false

    // dependencia de google
    id("com.google.gms.google-services") version "4.5.0" apply false
}