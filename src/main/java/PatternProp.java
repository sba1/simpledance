import gnu.gettext.GettextResource;

import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;

/**
 * @author Standard
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class PatternProp
{
	// *** BEGIN I18N
	static private ResourceBundle localeResource;
	static
	{
		try
		{
			localeResource = GettextResource.getBundle("SimpleDanceBundle");
		} catch(Exception e){};
	};
	static final private String _(String str)
	{
		if (localeResource == null) return str;
		return GettextResource.gettext(localeResource,str);
	}
	// *** END I18N

	private Shell parentShell;
	private Shell shell;
	private Combo typeCombo;
	private Text nameText;
	private Text barsText;
	private Text beatsText;
	private Text bpmText;
	private Text slowBeatText;
	
	private Button applyButton;
	private Button resetButton;
	private Button closeButton;
	
	private Pattern pattern;
	

	public PatternProp(Shell parentShell)
	{
		this.parentShell = parentShell;
	}
	
	public void open()
	{
		if (shell != null)
		{
			if (!shell.isDisposed())
			{
				return;
			}
		}

		shell = new Shell(parentShell);
		shell.setText("SimpleDance - " + _("Edit pattern properties"));
		
		shell.setLayout(new GridLayout(4,false));

		GridData gridData;
		Text text;
		Label label;

		label = new Label(shell,0);
		label.setText(_("Type"));

		typeCombo = new Combo(shell,SWT.READ_ONLY);
		for (int i=0;i<Pattern.DANCE_MAX;i++)
			typeCombo.add(_(Pattern.getTypeName(i)));
		typeCombo.select(0);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		typeCombo.setLayoutData(gridData);

		label = new Label(shell,0);
		label.setText(_("Name"));
		
		nameText = new Text(shell,SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		nameText.setLayoutData(gridData);

		label = new Label(shell,0);
		label.setText(_("Time signature"));

		barsText = new Text(shell,SWT.BORDER);
		barsText.addVerifyListener(new IntegerVerifyListener());
		gridData = new GridData();
		gridData.widthHint=20;
		barsText.setLayoutData(gridData);

		label = new Label(shell,0);
		label.setText("/");

		beatsText = new Text(shell,SWT.BORDER);
		beatsText.addVerifyListener(new IntegerVerifyListener());
		gridData = new GridData();
		gridData.widthHint=20;
		beatsText.setLayoutData(gridData);
		
		label = new Label(shell,0);
		label.setText(_("Bars per minute"));
		bpmText = new Text(shell,SWT.BORDER);
		bpmText.addVerifyListener(new IntegerVerifyListener());
		gridData = new GridData();
		gridData.widthHint=30;
		gridData.horizontalSpan = 3;
		bpmText.setLayoutData(gridData);
		
		label = new Label(shell,0);
		label.setText(_("Beats per slow step"));
		slowBeatText = new Text(shell,SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint = 30;
		gridData.horizontalSpan = 3;
		slowBeatText.setLayoutData(gridData);
		
		Composite buttonComposite = new Composite(shell,0);
		buttonComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		buttonComposite.setLayoutData(gridData);
		buttonComposite.setLayout(new GridLayout(3,false));
		applyButton = new Button(buttonComposite,0);
		applyButton.setText(_("Apply"));
		applyButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
		applyButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				if (pattern != null)
				{
					try
					{
						int bars = Integer.parseInt(barsText.getText());
						int beats = Integer.parseInt(beatsText.getText());
						int bpm = Integer.parseInt(bpmText.getText());
						int beatsPerSlowStep = Integer.parseInt(slowBeatText.getText());
						pattern.setTimeSignatureBars(bars);
						pattern.setTimeSignatureBeats(beats);
						pattern.setBarsPerMinute(bpm);
						pattern.setBeatsPerSlowStep(beatsPerSlowStep);
						pattern.setType(typeCombo.getSelectionIndex());
						pattern.setName(nameText.getText());
					} catch(Exception e)
					{
					}
				}
			}
		});

		resetButton = new Button(buttonComposite,0);
		resetButton.setText(_("Reset"));
		resetButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
		resetButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				setPattern(pattern);
			}
		});
		
		closeButton = new Button(buttonComposite,0);
		closeButton.setText(_("Close"));
		closeButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
		closeButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				close();
				setPattern(pattern);
			}

		});

		setPattern(pattern);
		shell.pack();
		shell.open();
	}
	
	public void close()
	{
		shell.close();
		shell.dispose();
		shell = null;
	}

	/**
	 * Sets the pattern.
	 * @param pattern The pattern to set
	 */
	public void setPattern(Pattern pattern)
	{
		this.pattern = pattern;
		if (pattern != null && shell != null && !shell.isDisposed())
		{
			barsText.setText(pattern.getTimeSignatureBars()+"");
			beatsText.setText(pattern.getTimeSignatureBeats()+"");
			bpmText.setText(pattern.getBarsPerMinute()+"");
			slowBeatText.setText(pattern.getBeatsPerSlowStep()+"");
			typeCombo.select(pattern.getType());
			nameText.setText(pattern.getName());
		}
	}

}
