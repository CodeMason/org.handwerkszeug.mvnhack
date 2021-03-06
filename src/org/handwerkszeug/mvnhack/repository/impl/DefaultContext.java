package org.handwerkszeug.mvnhack.repository.impl;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.handwerkszeug.common.util.Streams;
import org.handwerkszeug.common.util.UrlUtil;
import org.handwerkszeug.mvnhack.Constants;
import org.handwerkszeug.mvnhack.repository.Artifact;
import org.handwerkszeug.mvnhack.repository.Configuration;
import org.handwerkszeug.mvnhack.repository.Context;
import org.handwerkszeug.mvnhack.repository.Destination;
import org.handwerkszeug.mvnhack.repository.Repository;


public class DefaultContext implements Context {

	protected Map<String, Artifact> resolved;

	protected Configuration configuration;

	protected Map<String, String> managedDependencies;

	public DefaultContext(Configuration configuration) {
		this.configuration = configuration;
		this.resolved = new HashMap<String, Artifact>();
		this.managedDependencies = new HashMap<String, String>();
	}

	public DefaultContext(Configuration configuration,
			Map<String, Artifact> resolved) {
		this.configuration = configuration;
		this.resolved = resolved;
		this.managedDependencies = new HashMap<String, String>();
	}

	protected void addResolved(Artifact artifact) {
		resolved.put(toId(artifact.getGroupId(), artifact.getArtifactId(),
				artifact.getVersion()), artifact);
	}

	protected String toId(String groupId, String artifactId, String version) {
		return ArtifactUtil.toPath(groupId, artifactId, version, "");
	}

	@Override
	public void addManagedDependency(Artifact artifact) {
		this.managedDependencies.put(toManagedId(artifact), artifact
				.getVersion());
	}

	@Override
	public String getManagedDependency(Artifact artifact) {
		return this.managedDependencies.get(toManagedId(artifact));
	}

	protected String toManagedId(Artifact artifact) {
		return artifact.getGroupId() + '/' + artifact.getArtifactId();
	}

	@Override
	public Artifact resolve(String groupId, String artifactId, String version) {
		String id = toId(groupId, artifactId, version);
		Artifact result = resolved.get(id);
		if (result == null) {
			for (Repository r : configuration.getRepositories()) {
				Artifact a = r.load(this, groupId, artifactId, version);
				if (a != null) {
					for (Destination d : configuration.getDestinations()) {
						d.copyFrom(this, r, a);
					}
					addResolved(a);
					resolveDependencies(a);
					return a;
				}
			}
		}
		return result;
	}

	protected void resolveDependencies(Artifact a) {
		List<Artifact> list = new ArrayList<Artifact>();
		for (Artifact d : a.getDependencies()) {
			DefaultContext c = new DefaultContext(this.configuration,
					this.resolved);
			Artifact resolve = c.resolve(d.getGroupId(), d.getArtifactId(), d
					.getVersion());
			list.add(resolve);
		}
		a.getDependencies().clear();
		a.getDependencies().addAll(list);
	}

	@Override
	public InputStream open(Artifact artifact, URL url) {
		URL from = findLocal(artifact, url);
		Constants.LOG.log(Level.INFO, "read from {0}", from);
		InputStream result = UrlUtil.open(from);
		return result;
	}

	protected URL findLocal(Artifact artifact, URL url) {
		for (Destination d : configuration.getDestinations()) {
			File f = d.toDestination(artifact, url);
			if (f != null && f.exists()) {
				return UrlUtil.toURL(f);
			}
		}
		return url;
	}

	@Override
	public void close(InputStream stream) {
		Streams.close(stream);
	}
}
