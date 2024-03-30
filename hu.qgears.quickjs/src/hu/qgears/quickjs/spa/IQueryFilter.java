package hu.qgears.quickjs.spa;

import hu.qgears.quickjs.qpage.QQueryWrapper;

public interface IQueryFilter {
	Integer match(QQueryWrapper q, String path, int pathAt);
}
