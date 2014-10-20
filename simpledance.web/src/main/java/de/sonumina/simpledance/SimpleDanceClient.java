package de.sonumina.simpledance;

import org.teavm.dom.browser.Window;
import org.teavm.dom.html.HTMLDocument;
import org.teavm.dom.html.HTMLElement;
import org.teavm.jso.JS;

public class SimpleDanceClient {
    private static Window window = (Window)JS.getGlobal();
    private static HTMLDocument document = window.getDocument();

    public static void main(String[] args) {
        HTMLElement div = document.createElement("div");
        div.appendChild(document.createTextNode("SimpleDance"));
        document.getBody().appendChild(div);
    }
}
