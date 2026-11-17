# Project-specific R8 rules. The platform defaults come from proguard-android-optimize.txt (see build.gradle).

# SpotsDialog animates AnimatedView by property name ("xFactor") via reflection.
-keepclassmembers class dmax.plua.ui.dialog.AnimatedView {
    public float getXFactor();
    public void setXFactor(float);
}
