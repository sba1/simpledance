package de.sonumina.simpledance.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import de.sonumina.simpledance.core.graphics.Point;
import de.sonumina.simpledance.core.graphics.Render;
import de.sonumina.simpledance.core.graphics.Render.RenderSceneArgs;
import de.sonumina.simpledance.core.model.Pattern;

/**
 * The SimpleDance activity.
 *
 * @author Sebastian Bauer
 */
public class SimpleDanceActivity extends Activity
{
	static Pattern pattern = new Pattern();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(new BallroomView(this));
	}

	public class BallroomView extends View
	{
		int visibleLeft;
		int visibleTop;
		int zoomFactor = 100;
		int rotation;

		public BallroomView(Context context)
		{
			super(context);
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

		@Override
		protected void onDraw(Canvas canvas)
		{
			super.onDraw(canvas);

			Paint paint = new Paint();
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(android.graphics.Color.BLUE);
			canvas.drawPaint(paint);

			AndroidContext context = new AndroidContext(canvas);
			Render render = new Render(context);

			Render.ViewWholePatternResult result = render.viewWholePattern(getRenderSceneArgs());
			visibleLeft = result.visibleLeft;
			visibleTop = result.visibleTop;
			zoomFactor = result.zoomFactor;

			render.renderScence(getRenderSceneArgs());
		}
	}
}
