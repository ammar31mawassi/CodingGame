module com.codeescape {
    requires com.github.javaparser.core;
    requires javafx.controls;
    requires javafx.media;
    requires java.prefs;

    exports com.codeescape;
    exports com.codeescape.app;
    exports com.codeescape.engine;
    exports com.codeescape.model;
    exports com.codeescape.ui;
    exports com.codeescape.util;
    exports com.codeescape.validation;
}
