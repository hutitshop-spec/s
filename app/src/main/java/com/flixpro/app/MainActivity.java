package com.flixpro.app;

/**
 * Compatibility shim so an older WebView MainActivity at this path is overwritten
 * when upgrading an existing repository. The real native launcher is ui.MainActivity.
 */
@Deprecated
public class MainActivity extends com.flixpro.app.ui.MainActivity { }
