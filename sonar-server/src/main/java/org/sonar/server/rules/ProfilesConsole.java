/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.server.rules;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.ServerComponent;
import org.sonar.api.database.DatabaseSession;
import org.sonar.api.profiles.*;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.jpa.session.DatabaseSessionFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ProfilesConsole implements ServerComponent {

  private DatabaseSessionFactory sessionFactory;
  private RuleFinder ruleFinder;
  private List<ProfileExporter> profileExporters = new ArrayList<ProfileExporter>();
  private List<ProfileImporter> profileImporters = new ArrayList<ProfileImporter>();

  public ProfilesConsole(DatabaseSessionFactory sessionFactory, RuleFinder ruleFinder,
                         ProfileExporter[] exporters, DeprecatedProfileExporters deprecatedExporters,
                         ProfileImporter[] importers) {
    this.ruleFinder = ruleFinder;
    this.sessionFactory = sessionFactory;
    initProfileExporters(exporters, deprecatedExporters);
    this.profileImporters.addAll(Arrays.asList(importers));
  }

  private void initProfileExporters(ProfileExporter[] exporters, DeprecatedProfileExporters deprecatedExporters) {
    this.profileExporters.addAll(Arrays.asList(exporters));
    for (ProfileExporter exporter : deprecatedExporters.create()) {
      this.profileExporters.add(exporter);
    }
  }

  public String backupProfile(int profileId) {
    DatabaseSession session = sessionFactory.getSession();
    RulesProfile profile = loadProfile(session, profileId);
    if (profile != null) {
      Writer writer = new StringWriter();
      XMLProfileExporter.create().exportProfile(profile, writer);
      return writer.toString();
    }
    return null;
  }

  public ValidationMessages restoreProfile(String xmlBackup) {
    ValidationMessages messages = ValidationMessages.create();
    RulesProfile profile = XMLProfileImporter.create(ruleFinder).importProfile(new StringReader(xmlBackup), messages);
    if (profile != null) {
      DatabaseSession session = sessionFactory.getSession();
      RulesProfile existingProfile = session.getSingleResult(RulesProfile.class, "name", profile.getName(), "language", profile.getLanguage());
      if (existingProfile != null) {
        messages.addErrorText("The profile " + profile + " already exists. Please delete it before restoring.");
      } else if (!messages.hasErrors()) {
        session.saveWithoutFlush(profile);
        session.commit();
      }
    }
    return messages;
  }

  private RulesProfile loadProfile(DatabaseSession session, int profileId) {
    return session.getSingleResult(RulesProfile.class, "id", profileId);
  }

  public List<ProfileExporter> getProfileExportersForLanguage(String language) {
    List<ProfileExporter> result = new ArrayList<ProfileExporter>();
    for (ProfileExporter exporter : profileExporters) {
      if (exporter.getSupportedLanguages() == null || exporter.getSupportedLanguages().length == 0 || ArrayUtils.contains(exporter.getSupportedLanguages(), language)) {
        result.add(exporter);
      }
    }
    return result;
  }

  public List<ProfileImporter> getProfileImportersForLanguage(String language) {
    List<ProfileImporter> result = new ArrayList<ProfileImporter>();
    for (ProfileImporter importer : profileImporters) {
      if (importer.getSupportedLanguages() == null || importer.getSupportedLanguages().length == 0 || ArrayUtils.contains(importer.getSupportedLanguages(), language)) {
        result.add(importer);
      }
    }
    return result;
  }

  public String exportProfile(int profileId, String exporterKey) {
    DatabaseSession session = sessionFactory.getSession();
    RulesProfile profile = loadProfile(session, profileId);
    if (profile != null) {
      ProfileExporter exporter = getProfileExporter(exporterKey);
      Writer writer = new StringWriter();
      exporter.exportProfile(profile, writer);
      return writer.toString();
    }
    return null;
  }

  /**
   * Important : the ruby controller has already removed existing profile with same name/language.
   */
  public ValidationMessages importProfile(String profileName, String language, String importerKey, String profileDefinition) {
    ValidationMessages messages = ValidationMessages.create();
    ProfileImporter importer = getProfileImporter(importerKey);
    RulesProfile profile = importer.importProfile(new StringReader(profileDefinition), messages);
    if (!messages.hasErrors()) {
      profile.setName(profileName);
      profile.setLanguage(language);
      DatabaseSession session = sessionFactory.getSession();
      session.saveWithoutFlush(profile);
      session.commit();
    }
    return messages;
  }

  public ProfileExporter getProfileExporter(String exporterKey) {
    for (ProfileExporter exporter : profileExporters) {
      if (StringUtils.equals(exporterKey, exporter.getKey())) {
        return exporter;
      }
    }
    return null;
  }

  public ProfileImporter getProfileImporter(String exporterKey) {
    for (ProfileImporter importer : profileImporters) {
      if (StringUtils.equals(exporterKey, importer.getKey())) {
        return importer;
      }
    }
    return null;
  }
}