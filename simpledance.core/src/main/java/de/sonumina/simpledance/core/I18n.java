package de.sonumina.simpledance.core;

import gnu.gettext.GettextResource;

import java.util.ResourceBundle;

/**
 * Wrapper for libintl.jar.
 *
 * @author Sebastian Bauer
 */
public class I18n
{
	static private ResourceBundle localeResource;
	static private boolean localeResourceAttempted;

	static final private void initLocaleResource()
	{
		try
		{
			localeResource = GettextResource.getBundle("SimpleDanceBundle");
		} catch(Exception e){};
		localeResourceAttempted = true;
	};

	static final public String _(String str)
	{
		if (!localeResourceAttempted) initLocaleResource();
		if (localeResource == null) return str;
		return GettextResource.gettext(localeResource,str);
	}

	static final public String N_(String str)
	{
		return str;
	}
}
