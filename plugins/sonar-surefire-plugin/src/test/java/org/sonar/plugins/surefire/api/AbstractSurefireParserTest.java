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
package org.sonar.plugins.surefire.api;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.*;
import org.sonar.api.test.IsMeasure;
import org.sonar.api.test.IsResource;

import java.net.URISyntaxException;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AbstractSurefireParserTest {

  @Test
  public void shouldAggregateReports() throws URISyntaxException {
    AbstractSurefireParser parser = newParser();
    SensorContext context = mockContext();

    parser.collect(new Project("foo"), context, getDir("multipleReports"));

    verify(context, times(6)).saveMeasure(argThat(new IsResource(Scopes.FILE, Qualifiers.FILE)), eq(CoreMetrics.TESTS), anyDouble());
    verify(context, times(6)).saveMeasure(argThat(new IsResource(Scopes.FILE, Qualifiers.FILE)), eq(CoreMetrics.TEST_ERRORS), anyDouble());
    verify(context, times(6)).saveMeasure(argThat(new IsResource(Scopes.FILE, Qualifiers.FILE)), argThat(new IsMeasure(CoreMetrics.TEST_DATA)));
  }

  /**
   * See http://jira.codehaus.org/browse/SONAR-2371
   */
  @Test
  public void shouldInsertZeroWhenNoReports() throws URISyntaxException {
    AbstractSurefireParser parser = newParser();
    SensorContext context = mockContext();
    Project project = mock(Project.class);

    parser.collect(project, context, getDir("noReports"));

    verify(context).saveMeasure(CoreMetrics.TESTS, 0.0);
  }

  /**
   * See http://jira.codehaus.org/browse/SONAR-2371
   */
  @Test
  public void shouldNotInsertZeroWhenNoReports() throws URISyntaxException {
    AbstractSurefireParser parser = newParser();
    SensorContext context = mockContext();
    Project project = mock(Project.class);
    when(project.getModules()).thenReturn(Arrays.asList(new Project("foo")));

    parser.collect(project, context, getDir("noReports"));

    verify(context, never()).saveMeasure(CoreMetrics.TESTS, 0.0);
  }

  @Test
  public void shouldNotInsertZeroOnFiles() throws URISyntaxException {
    AbstractSurefireParser parser = newParser();
    SensorContext context = mockContext();

    parser.collect(new Project("foo"), context, getDir("noTests"));

    verify(context, never()).saveMeasure(any(Resource.class),(Metric)anyObject(), anyDouble());
  }

  @Test
  public void shouldMergeInnerClasses() throws URISyntaxException {
    AbstractSurefireParser parser = newParser();

    SensorContext context = mock(SensorContext.class);
    when(context.isIndexed(argThat(new BaseMatcher<Resource>(){
      public boolean matches(Object o) {
        return !((Resource)o).getName().contains("$");
      }
      public void describeTo(Description description) {
      }
    }), eq(false))).thenReturn(true);

    parser.collect(new Project("foo"), context, getDir("innerClasses"));

    verify(context).saveMeasure(argThat(new IsResource(Scopes.FILE, Qualifiers.FILE, "org.apache.commons.collections.bidimap.AbstractTestBidiMap")), eq(CoreMetrics.TESTS), eq(7.0));
    verify(context).saveMeasure(argThat(new IsResource(Scopes.FILE, Qualifiers.FILE, "org.apache.commons.collections.bidimap.AbstractTestBidiMap")), eq(CoreMetrics.TEST_ERRORS), eq(1.0));
    verify(context, never()).saveMeasure(argThat(new IsResource(Scopes.FILE, Qualifiers.FILE, "org.apache.commons.collections.bidimap.AbstractTestBidiMap$TestBidiMapEntrySet")), any(Metric.class), anyDouble());
  }


  private AbstractSurefireParser newParser() {
    return new AbstractSurefireParser() {
      @Override
      protected Resource<?> getUnitTestResource(String classKey) {
        return new File(classKey);
      }
    };
  }

  private java.io.File getDir(String dirname) throws URISyntaxException {
    return new java.io.File(getClass().getResource("/org/sonar/plugins/surefire/api/AbstractSurefireParserTest/" + dirname).toURI());
  }

  private SensorContext mockContext() {
    SensorContext context = mock(SensorContext.class);
    when(context.isIndexed(any(Resource.class), eq(false))).thenReturn(true);
    return context;
  }
}
