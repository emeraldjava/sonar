/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
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
package org.sonar.plugins.core.i18n;

import org.sonar.api.i18n.LanguagePack;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class QuebecLanguagePack extends LanguagePack {

  @Override
  public List<String> getPluginKeys() {
    return Arrays.asList("test");
  }

  @Override
  public List<Locale> getLocales() {
    return Arrays.asList(Locale.CANADA_FRENCH);
  }
}