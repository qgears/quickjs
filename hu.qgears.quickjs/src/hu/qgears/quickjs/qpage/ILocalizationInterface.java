package hu.qgears.quickjs.qpage;

import java.util.Date;

import hu.qgears.commons.UtilString;

public interface ILocalizationInterface {
	default String getString(String id, Object[] args)
	{
		StringBuilder ret=new StringBuilder();
		ret.append("<");
		ret.append(id);
		for(Object arg: args)
		{
			ret.append(",");
			ret.append(""+arg);
		}
		ret.append(">");
		return ret.toString();
	}
	default String formatDate(Date commitDate)
	{
		return ""+commitDate;
	}
	default String formatDateLong(Date commitDate)
	{
		return ""+commitDate;
	}
	/**
	 * Format decimal number with given number of fractional digits
	 * @param d
	 * @param i
	 * @return
	 */
	default String formatDecimal(double d, int i)
	{
		return String.format("%."+i+"f",d);
	}
	default String formatMinuteWithinDay(int i)
	{
		int hr=i/60;
		int min=i%60;
		String time=UtilString.fillLeft(""+hr, 2, '0')+":"+UtilString.fillLeft(""+min, 2, '0');
		return time;
	}
	default Double parseDecimal(String decimal)
	{
		String replaced=decimal.replaceAll(",", ".");
		return Double.parseDouble(replaced);
	}
}
