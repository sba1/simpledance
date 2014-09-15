package de.sonumina.simpledance;

import gnu.gettext.GettextResource;

import java.util.ResourceBundle;

/**
 * Wrapper for libintl.jar.
 *
 * @author Sebastian Bauer
 */
public class I18n
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

	static final public String _(String str)
	{
		if (localeResource == null) return str;
		return GettextResource.gettext(localeResource,str);
	}

	static final public String N_(String str)
	{
		return str;
	}
}
