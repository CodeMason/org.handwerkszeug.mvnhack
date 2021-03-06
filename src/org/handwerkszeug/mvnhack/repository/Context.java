package org.handwerkszeug.mvnhack.repository;

import java.io.InputStream;
import java.net.URL;

public interface Context {

	/**
	 * @notnull groupId
	 * @notnull artifactId
	 * @notnull version
	 * @return maybe null
	 */
	Artifact resolve(String groupId, String artifactId, String version);

	/**
	 * @notnull artifact
	 */
	void addManagedDependency(Artifact artifact);

	/**
	 * @notnull artifact
	 * @return version
	 */
	String getManagedDependency(Artifact artifact);

	/**
	 * @notnull artifact
	 * @notnull url
	 * @return maybe null
	 */
	InputStream open(Artifact artifact, URL url);

	/**
	 * @param stream
	 */
	void close(InputStream stream);

}
