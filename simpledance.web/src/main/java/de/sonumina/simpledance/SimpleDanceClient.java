package de.sonumina.simpledance;

import org.teavm.dom.browser.Window;
import org.teavm.dom.canvas.CanvasRenderingContext2D;
import org.teavm.dom.html.HTMLCanvasElement;
import org.teavm.dom.html.HTMLDocument;
import org.teavm.dom.html.HTMLElement;
import org.teavm.jso.JS;

import de.sonumina.simpledance.core.graphics.Point;
import de.sonumina.simpledance.core.graphics.Render;
import de.sonumina.simpledance.core.graphics.Render.RenderSceneArgs;
import de.sonumina.simpledance.core.graphics.Render.ViewWholePatternResult;
import de.sonumina.simpledance.core.model.Pattern;

public class SimpleDanceClient
{
	private static Window window = (Window)JS.getGlobal();
	private static HTMLDocument document = window.getDocument();

	private Pattern pattern = new Pattern();
	private Render render;

	private int visibleLeft;
	private int visibleTop;
	private int zoomFactor = 100;
	private int rotation;

	private int getWidth()
	{
		return 200;
	}

	private int getHeight()
	{
		return 200;
	}

	private RenderSceneArgs getRenderSceneArgs()
	{
		RenderSceneArgs rsa = new RenderSceneArgs();
		rsa.pattern = pattern;
		rsa.stepNumber = pattern.getCurrentStepNum();
		int visibleWidth = getWidth() * 100 / zoomFactor;
		int visibleHeight = getHeight() * 100 / zoomFactor;
		rsa.visibleLeftTop = new Point(visibleLeft, visibleTop);
		rsa.visibleRightTop = rsa.visibleLeftTop.add(new Point(visibleWidth, 0).rotate(rotation));
		rsa.visibleLeftBottom = rsa.visibleLeftTop.add(new Point(0, -visibleHeight).rotate(rotation));
		rsa.visibleRightBottom = rsa.visibleLeftTop.add(new Point(visibleWidth, -visibleHeight).rotate(rotation));
		rsa.visibleRotation = rotation;
		rsa.pixelWidth = getWidth();
		rsa.pixelHeight = getHeight();
		rsa.showGent = true;
		rsa.showLady = true;
		return rsa;
	}

	public SimpleDanceClient(CanvasRenderingContext2D context)
	{
		render = new Render(new CanvasContext(context));
		ViewWholePatternResult result = render.viewWholePattern(getRenderSceneArgs());
		visibleLeft = result.visibleLeft;
		visibleTop = result.visibleTop;
		zoomFactor = result.zoomFactor;
	}

	public static void main(String[] args)
	{
		HTMLElement div = document.createElement("div");
		HTMLCanvasElement canvas = (HTMLCanvasElement) document.createElement("canvas");
		CanvasRenderingContext2D context = (CanvasRenderingContext2D)canvas.getContext("2d");
		new SimpleDanceClient(context);
		div.appendChild(canvas);
		document.getBody().appendChild(div);
	}
}
