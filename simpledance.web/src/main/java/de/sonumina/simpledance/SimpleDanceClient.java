package de.sonumina.simpledance;

import org.teavm.dom.browser.Window;
import org.teavm.dom.canvas.CanvasRenderingContext2D;
import org.teavm.dom.events.Event;
import org.teavm.dom.events.EventListener;
import org.teavm.dom.events.MouseEvent;
import org.teavm.dom.html.HTMLCanvasElement;
import org.teavm.dom.html.HTMLDocument;
import org.teavm.jso.JS;
import org.teavm.jso.JSProperty;

import de.sonumina.simpledance.core.graphics.InputContext;
import de.sonumina.simpledance.core.graphics.Point;
import de.sonumina.simpledance.core.graphics.Render;
import de.sonumina.simpledance.core.graphics.Render.RenderSceneArgs;
import de.sonumina.simpledance.core.graphics.Render.ViewWholePatternResult;
import de.sonumina.simpledance.core.model.Pattern;

public class SimpleDanceClient
{
	public static interface MyWindow extends Window
	{
		@JSProperty
		int getInnerWidth();

		@JSProperty
		int getInnerHeight();

		void addEventListener(String type, EventListener listener);
	}

	private static MyWindow window = (MyWindow)JS.getGlobal();
	private static HTMLDocument document = window.getDocument();

	private Pattern pattern = new Pattern();
	private Render render;
	private HTMLCanvasElement canvas;

	private int visibleLeft;
	private int visibleTop;
	private int zoomFactor = 100;
	private int rotation;
	private InputContext inputContext = new InputContext();

	private int getWidth()
	{
		return canvas.getWidth();
	}

	private int getHeight()
	{
		return canvas.getHeight();
	}

	private HTMLCanvasElement getCanvas()
	{
		return canvas;
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

	private void updateCanvasSizes()
	{
		canvas.setWidth(window.getInnerWidth());
		canvas.setHeight(window.getInnerHeight());
	}

	public SimpleDanceClient()
	{
		canvas = (HTMLCanvasElement) document.createElement("canvas");
		canvas.setAttribute("style", "display: block;");
		window.addEventListener("resize", new EventListener()
		{
			@Override
			public void handleEvent(Event evt)
			{
				updateCanvasSizes();
				render.renderScence(getRenderSceneArgs());
			}
		});

		canvas.addEventListener("click", new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				MouseEvent mevt = (MouseEvent)evt;
				if (mevt.getButton() != 0) return;
				render.mouseDown(getRenderSceneArgs(), inputContext, mevt.getClientX(), mevt.getClientY());
			}
		});

		canvas.addEventListener("mousemove", new EventListener() {
			@Override
			public void handleEvent(Event evt)
			{
				if (inputContext.mousePressed)
				{
					MouseEvent mevt = (MouseEvent)evt;
					render.mouseMove(getRenderSceneArgs(), inputContext, mevt.getClientX(), mevt.getClientY());
				}
			}
		});

		updateCanvasSizes();
		CanvasRenderingContext2D context = (CanvasRenderingContext2D)canvas.getContext("2d");
		render = new Render(new CanvasContext(context));
		ViewWholePatternResult result = render.viewWholePattern(getRenderSceneArgs());
		visibleLeft = result.visibleLeft;
		visibleTop = result.visibleTop;
		zoomFactor = result.zoomFactor;
		render.renderScence(getRenderSceneArgs());
	}

	public static void main(String[] args)
	{
		SimpleDanceClient client = new SimpleDanceClient();
		document.getBody().setAttribute("style", "margin: 0px;");
		document.getBody().appendChild(client.getCanvas());
	}
}
