/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.report.web;

import org.openlmis.report.dto.RequisitionReportDto;
import org.openlmis.report.dto.external.RequisitionDto;
import org.openlmis.report.dto.external.RequisitionLineItemDto;
import org.openlmis.report.dto.external.RequisitionStatusDto;
import org.openlmis.report.dto.external.StatusChangeDto;
import org.openlmis.report.dto.external.UserDto;
import org.openlmis.report.i18n.MessageKeys;
import org.openlmis.report.i18n.MessageService;
import org.openlmis.report.service.referencedata.UserReferenceDataService;
import org.openlmis.report.utils.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequisitionReportDtoBuilderTest {

  private static final String SYSTEM = "SYSTEM";
  private static final BigDecimal TOTAL_COST = new BigDecimal("15.6");
  private static final BigDecimal FS_TOTAL_COST = new BigDecimal("3");
  private static final BigDecimal NFS_TOTAL_COST = new BigDecimal("22.8");

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Mock
  private MessageService messageService;

  @Mock
  private RequisitionDto requisition;

  @Mock
  private UserDto user1;

  @Mock
  private UserDto user2;

  @Mock
  private List<RequisitionLineItemDto> fullSupply;

  @Mock
  private List<RequisitionLineItemDto> nonFullSupply;

  private UUID userId1 = UUID.randomUUID();
  private UUID userId2 = UUID.randomUUID();

  @InjectMocks
  private RequisitionReportDtoBuilder requisitionReportDtoBuilder =
      new RequisitionReportDtoBuilder();

  @Before
  public void setUp() {
    when(user1.getId()).thenReturn(userId1);
    when(user2.getId()).thenReturn(userId2);
    when(userReferenceDataService.findOne(userId1)).thenReturn(user1);
    when(userReferenceDataService.findOne(userId2)).thenReturn(user2);

    when(requisition.getNonSkippedFullSupplyRequisitionLineItems())
        .thenReturn(fullSupply);
    when(requisition.getNonSkippedNonFullSupplyRequisitionLineItems())
        .thenReturn(nonFullSupply);

    Message msg = new Message(MessageKeys.STATUS_CHANGE_USER_SYSTEM);
    when(messageService.localize(msg))
      .thenReturn(msg. new LocalizedMessage(SYSTEM));

    when(requisition.getTotalCost()).thenReturn(TOTAL_COST);
    when(requisition.getFullSupplyTotalCost()).thenReturn(FS_TOTAL_COST);
    when(requisition.getNonFullSupplyTotalCost()).thenReturn(NFS_TOTAL_COST);
  }

  @Test
  public void shouldBuildDtoWithoutStatusChanges() {
    RequisitionReportDto dto = requisitionReportDtoBuilder.build(requisition);

    commonReportDtoAsserts(dto);
    assertNull(dto.getInitiatedBy());
    assertNull(dto.getInitiatedDate());
    assertNull(dto.getSubmittedBy());
    assertNull(dto.getSubmittedDate());
    assertNull(dto.getAuthorizedBy());
    assertNull(dto.getAuthorizedDate());
  }

  @Test
  public void shouldBuildDtoWithStatusChanges() {
    ZonedDateTime initDt = ZonedDateTime.now().minusDays(11);
    ZonedDateTime submitDt = ZonedDateTime.now().minusDays(6);
    ZonedDateTime authorizeDt = ZonedDateTime.now().minusDays(2);
    StatusChangeDto initStatusChange = mock(StatusChangeDto.class);
    StatusChangeDto submitStatusChange = mock(StatusChangeDto.class);
    StatusChangeDto authorizeStatusChange = mock(StatusChangeDto.class);
    when(initStatusChange.getStatus()).thenReturn(RequisitionStatusDto.INITIATED);
    when(initStatusChange.getCreatedDate()).thenReturn(initDt);
    when(initStatusChange.getAuthorId()).thenReturn(userId1);
    when(submitStatusChange.getStatus()).thenReturn(RequisitionStatusDto.SUBMITTED);
    when(submitStatusChange.getCreatedDate()).thenReturn(submitDt);
    when(submitStatusChange.getAuthorId()).thenReturn(userId2);
    when(authorizeStatusChange.getStatus()).thenReturn(RequisitionStatusDto.AUTHORIZED);
    when(authorizeStatusChange.getCreatedDate()).thenReturn(authorizeDt);
    when(authorizeStatusChange.getAuthorId()).thenReturn(userId1);
    List<StatusChangeDto> statusChanges = new ArrayList<>();
    statusChanges.add(initStatusChange);
    statusChanges.add(submitStatusChange);
    statusChanges.add(authorizeStatusChange);
    when(requisition.getStatusHistory()).thenReturn(statusChanges);

    RequisitionReportDto dto = requisitionReportDtoBuilder.build(requisition);

    commonReportDtoAsserts(dto);
    assertEquals(user1, dto.getInitiatedBy());
    assertEquals(initDt, dto.getInitiatedDate());
    assertEquals(user2, dto.getSubmittedBy());
    assertEquals(submitDt, dto.getSubmittedDate());
    assertEquals(user1, dto.getAuthorizedBy());
    assertEquals(authorizeDt, dto.getAuthorizedDate());
  }

  @Test
  public void shouldBuildDtoWithSystemStatusChange() {
    ZonedDateTime now = ZonedDateTime.now();
    StatusChangeDto initStatusChange = mock(StatusChangeDto.class);
    when(initStatusChange.getStatus()).thenReturn(RequisitionStatusDto.INITIATED);
    when(initStatusChange.getCreatedDate()).thenReturn(now);
    List<StatusChangeDto> statusChanges = Collections.singletonList(initStatusChange);
    when(requisition.getStatusHistory()).thenReturn(statusChanges);

    RequisitionReportDto dto = requisitionReportDtoBuilder.build(requisition);

    commonReportDtoAsserts(dto);
    assertNull(dto.getSubmittedBy());
    assertNull(dto.getSubmittedDate());
    assertNull(dto.getAuthorizedBy());
    assertNull(dto.getAuthorizedDate());

    assertEquals(now, dto.getInitiatedDate());
    UserDto fakeUser = dto.getInitiatedBy();
    assertNotNull(fakeUser);
    assertEquals(SYSTEM, fakeUser.getFirstName());
    assertNull(fakeUser.getLastName());
    assertEquals(SYSTEM, fakeUser.getUsername());
  }

  private void commonReportDtoAsserts(RequisitionReportDto dto) {
    assertEquals(requisition, dto.getRequisition());
    assertEquals(fullSupply, dto.getFullSupply());
    assertEquals(nonFullSupply, dto.getNonFullSupply());
    assertEquals(TOTAL_COST, dto.getTotalCost());
    assertEquals(FS_TOTAL_COST, dto.getFullSupplyTotalCost());
    assertEquals(NFS_TOTAL_COST, dto.getNonFullSupplyTotalCost());
  }
}
