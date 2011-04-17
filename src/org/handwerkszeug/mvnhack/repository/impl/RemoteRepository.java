package org.handwerkszeug.mvnhack.repository.impl;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.handwerkszeug.common.exception.IORuntimeException;
import org.handwerkszeug.common.util.UrlUtil;
import org.handwerkszeug.mvnhack.Constants;
import org.handwerkszeug.mvnhack.repository.Artifact;
import org.handwerkszeug.mvnhack.repository.ArtifactBuilder;
import org.handwerkszeug.mvnhack.repository.Context;
import org.handwerkszeug.mvnhack.repository.Repository;

public class RemoteRepository implements Repository {

	protected String baseUrl;

	protected ArtifactBuilder builder;

	public RemoteRepository(String url, ArtifactBuilder builder) {
		if (url.endsWith("/") == false) {
			url += "/";
		}
		this.baseUrl = url;
		this.builder = builder;
	}

	@Override
	public Artifact load(Context context, String groupId, String artifactId,
			String version) {
		StringBuilder stb = new StringBuilder();
		stb.append(baseUrl);
		stb.append(ArtifactUtil.toPom(groupId, artifactId, version));
		URL url = UrlUtil.toURL(stb.toString());
		try {
			return builder.build(context, context.open(
					ArtifactUtil.create(groupId, artifactId, version), url));
		} catch (IORuntimeException e) {
		}
		return null;
	}

	@Override
	public Set<URL> getLocation(Artifact artifact) {
		Set<URL> urls = new HashSet<URL>();
		urls.add(toURL(ArtifactUtil.toPath(artifact)));
		urls.add(toURL(ArtifactUtil.toPath(artifact, ".jar")));
		urls.add(toURL(ArtifactUtil.toPath(artifact, Constants.POM)));
		urls.add(toURL(ArtifactUtil.toPath(artifact,
				"-sources." + artifact.getType())));
		urls.add(toURL(ArtifactUtil.toPath(artifact, "-sources.jar")));
		return urls;
	}

	protected URL toURL(String suffix) {
		StringBuilder stb = new StringBuilder();
		stb.append(this.baseUrl);
		stb.append(suffix);
		return UrlUtil.toURL(stb.toString());
	}
}
