<html>
	<#assign year>
    ${catalogYearTerm?substring(0,4)}
    </#assign>
    <#assign numTerm>
    ${catalogYearTerm?substring(4,5)}
    </#assign>
    <#assign term="NONE">
    <#if numTerm?string?contains("1")>
        <#assign term="WIN">
    <#elseif numTerm?string?contains("2")>
        <#assign term="SPR">
    <#elseif numTerm?string?contains("3")>
        <#assign term="SUM">
    <#elseif numTerm?string?contains("4")>
        <#assign term="AUT">
    </#if>
<div class="myplan-audit-report">
	<h1>${dpTitle1?xml}</h1>   
	<table class="audit-summary">
		<tr>
			<td colspan="2">
				<table>	
					<#if degreeDate?trim != "NotFound">
					<tr>
						<td class="graduation-date">
							<label>Gradutation Date:</label> ${degreeDate?replace("/", " ")}
						</td>
					</tr>
					</#if>
					<tr>
						<td class="program-entry-qtr">
							<label>Program Entry Qtr:</label> ${term} ${year}
						</td>
					</tr>
					<tr>
						<td class="date-prepared">
							<label>Date Prepared:</label> ${preparedDate}
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
<!-- hardcoded top legalese
	<p>This report is an advising tool used to track degree progress. Final confirmation of degree requirements is subject to department or college and university approval.</p>
-->	
	<!--
	<#if showTestMessage>
	<div id="testMessageHeader">
	    ${testMessage?xml}
	</div>
	</#if>
	-->
	<!-- should be this stored text instead of the hardcoded paragraph above  -->
	<div class="fl-text-align-center">
	      <#list includeTopText as topTextLine>
	        ${topTextLine?replace("*", "")?trim} </#list>
	</div>
	
	<!--
	<#if showRefArtHeader>
	<div id="refArtHeaderText">
	    ${refArtHeaderTextLine1?xml}
	    ${refArtHeaderTextLine2?xml}
	</div>
	</#if>
	-->
<#if auditStatus = -1>
	<div class="audit-incomplete"><br/>NOTE: At least one requirement still incomplete.<br/></div>
</#if>

<#assign inSection = false>
<#assign needsDropdown = true>

<#list auditReportReqs as req>
		
	<#-- list of known advisory text rnames. just add any missing rnames over time. -->
<#assign x = [
	"1BIOLADV",
	"1EEADV",
	"2CSSADVIS",
	"AAOFFICE",
	"ACMSADVIS",
	"AESADVIS",
	"AFSADVIS",
	"AISADVIS",
	"ANTHADVIS",
	"ARCHADVIS",
	"ARTADVISE",
	"ARTHADVIS",
	"ASIANADV",
	"ASTRADVIS",
	"BA-ADVIS",
	"BE-ADVIS",
	"BIASADVIS",
	"BIOCHMADV",
	"BIOLADVIS",
	"CEPADVIS",
	"CHEMADV1",
	"CHEMEOFFC",
	"CHIDADV",
	"CIVEADVIS",
	"CLASADVIS",
	"CLITADV",
	"CMADVIS",
	"CMUADVIS",
	"COMADVIS",
	"CSCIADV",
	"DANCEADV",
	"DC-INFO",
	"DRAMAADV",
	"DXADVISE",
	"ECFS-ADV",
	"ECONADVIS",
	"EEOFFICE",
	"ENGLADVIS",
	"ENVHADV",
	"ENVIRADV",
	"ESS-ADV",
	"ETHNOADVS",
	"EVEADV",
	"FORADVISE",
	"FRENCHADV",
	"GENSTADV",
	"GEOGADVIS",
	"GERMADVIS",
	"GSDNPADV",
	"GSMBAADV",
	"GSNBBADV",
	"GWSSADVS",
	"HCD-ADVIS",
	"HI-ADV",
	"HISTADVIS",
	"INDEOFFIC",
	"INFORMADV",
	"ITALADVIS",
	"LARCHADV",
	"LINGADVIS",
	"LSJADVIS",
	"MATHADVIS",
	"MEDTADVIS",
	"MEDXDADV",
	"MEOFFICE",
	"MICRADVIS",
	"MINORADVS",
	"MUSICADVS",
	"NBIOADVIS",
	"NELCADVIS",
	"NURSADV",
	"OCEANADVS",
	"PHARMADV",
	"PHILADVIS",
	"PHILADVIS",
	"PHYSADVIS",
	"PM-ADV-TX",
	"POLSADVIS",
	"PROADVIS",
	"PSYCHADVI",
	"SCANDADV",
	"SISADVIS",
	"SLAVADVIS",
	"SOCADVIS",
	"SOCWFADVS",
	"SPANADV",
	"SPCMUADVS",
	"SPHSCADVS",
	"STATADVIS",
	"WHAT-IF",
	"WHAT-IFM",
	"WOMENADVS"
]>

<#if x?seq_contains(req.rname?substring(7)?trim)>
    <div class="advisory ${req.rname?xml} ${req.satisfied?xml}"> 
	<#list req.headerLines as headerLine> 
		${headerLine?replace("*", "")?replace("NOTE:", "<br/>NOTE:")?trim} 
	</#list> 
	</div>
<#elseif ( req.headerLines?size > 0 ) >	
	
	
	<#if inSection>
	</div>
	</#if> 

	<div class="section ${req.rname?xml} ${req.satisfied?xml}"> 
	<#assign inSection = true>
	<#if needsDropdown >
		<div class="control-toolbar">
		<#assign needsDropdown = false>
			<label for="requirement-status">Show</label>
			<select id="requirement-status">
				<option value="all">All Requirements</option>
				<option value="Status_NO">Incomplete Only</option>
				<option value="Status_IP">In-Progress Only</option>	
				<!-- <option value="Status_OK">Complete Only</option> -->				
				<!-- <option value="Status_PL">Plan Only</option> -->
			</select>
		</div>
        <div class="myplan-status alert uif-boxLayoutVerticalItem audit-filtered" style="margin-bottom:20px; float:none; display:none;">
            <img src="/student/ks-myplan/images/pixel.gif" alt="" class="icon">
            <div class="message">You are viewing a partial degree audit report. See &#39;All Requirements&#39; for the full audit report.</div>
        </div>
	</#if>

		<#list req.headerLines as headerLine> 
			<div class="heading">${headerLine?replace("*", "")?replace("_", "")?replace(". NOTE:", ".<br/>NOTE:")?trim}</div>
            <div class="myplan-status info uif-boxLayoutVerticalItem all-reqs-filtered" style="margin-bottom:20px; float:none; display:none;">
                <img src="/student/ks-myplan/images/pixel.gif" alt="" class="icon">
                <div class="message">All requirements in this section have been hidden. See &#39;All Requirements&#39; for the full audit report.</div>
            </div>
		</#list>

<#else>
		
	<#assign reqStatus = "Status_NONE" >
	<#assign reqAbbrv = "??" >

	<#if req.showStatus >
		<#assign reqStatus = "${req.satisfied?xml}" >
		<#if reqStatus = "Status_NO" >
			<#assign reqAbbrv = "NO" >
		</#if>
		<#if reqStatus = "Status_IP" >
			<#assign reqAbbrv = "IP" >
		</#if>
		<#if reqStatus = "Status_OK" >
			<#assign reqAbbrv = "OK" >
		</#if>
	</#if>	
				
		<div class="requirement ${req.rname?xml} ${reqStatus} ${req.summary?xml}">
			<#if req.showTitle>
			<#if !req.titleLines[0]?contains("________________")>
			<div class="header">
				<div class="toggle"> </div>
				<span class="${reqStatus}">${reqAbbrv}</span>
				<div class="title"><#list req.titleLines as titleLine>${titleLine?replace("*NOTE:", "<br/>NOTE:")?replace("*", "")?replace("_","")?replace(". NOTE:", ".<br/>NOTE:")?trim} </#list></div>
			</div>
			</#if>
			</#if>

			<#if req.showGotSummary || req.showInProgressHours || req.showNeedsSummary >
	        <div class="totals">
				<#if req.showGotSummary>
	            <span class="earned">
	                <#if req.showWarnInd>${req.warnInd?xml}</#if>
	                Earned:
	            	<#if req.showGotHours>
	            		<span class="value">${req.gotHours?xml}</span> ${req.gotHoursText?xml}
					</#if>
	            	<#if req.showGotCount>
	            		<span class="value">${req.gotCount?xml}</span> ${req.gotCountText?xml}
	            	</#if>
	                <#if req.showGotSubreqs>
	                	<span class="value">${req.gotSubreqs?xml}</span> ${req.gotSubreqsText?xml}
	                </#if>
	                <#if req.showGotGpa>
	                	<span class="value">${req.gotGpa?xml}</span> GPA
	                </#if>
	            </span>
	            </#if> 
				<!--
	            <#if req.showDetailGpaLine>
	            <span class="reqGpaDetail">
	            	${req.gotGpaHours} 
	            	${req.gotGpaHoursText} 
	            	${req.gotGpaPoints}
					${req.gotGpaPointsText}
					${req.gotGpa} GPA
	            </div>
	            </#if>
				-->
				<#if req.showInProgressHours>
	            <span class="inprogress"> 
	            	In-progress: 
	            	<span class="value"> ${req.ipHours?xml} </span> ${req.ipHoursText?xml} 
	            </span>
	            </#if>
				<!--                
	            <#if req.showPlannedHours>
	            <div class="whatif">
	                ${req.wifStub?xml}
	                <span class="value">${req.wifHours?xml} </span> ${req.wifHoursText?xml}
	            </div>
				</#if>
				-->                
	            <#if req.showNeedsSummary>
	            <span class="needs">
	                Needs:
					<#if req.showNeedsHours>
						<span class="value"> ${req.needsHours?xml} </span> ${req.needsHoursText?xml}
					</#if>
	            	<#if req.showNeedsCount>
	            		<span class="value"> ${req.needsCount?xml} </span> ${req.needsCountText?xml}
	            	</#if>
	                <#if req.showNeedsSubreqs>
	                	<span class="value"> ${req.needsSubreqs?xml} </span> ${req.needsSubreqsText?xml}
	                </#if>
	                <#if req.showNeedsGpa>
	                	<span class="value"> ${req.needsGpa?xml} </span> GPA
	                </#if>
	            </span>
	            </#if>
	        </div> <!-- end of totals -->
	        </#if>
	            
			<#if req.showExcLines>
			<div class="exceptions">
				<#list req.appliedExceptionText as ex> <span class="reqCline">${ex?xml}</span></#list>
			</div>
			</#if>
			
			<#list req.auditReportSubreqs as subreq>
			<div class="subrequirement">

				<#if subreq.showSubreqStatus>
				<span class="error">${subreq.seqErr?xml}</span>
				<span class="status ${subreq.status?xml}"></span>
				</#if>
				<#if subreq.required>
				<span class="required">${subreq.subreqRequired?xml}</span>
				</#if>
				<#if subreq.showTitle >
				<div class="title">
					<#if subreq.showSubreqNumber>
					<span class="subreqNumber">${subreq.subreqNumber?xml}<#if subreq.showParen>)</#if></span>
					</#if>
					<#list subreq.titleLines as titleLine>${titleLine?replace("*NOTE:", "<br/>NOTE:")?replace("*", "")?replace("_", "")?replace(". NOTE:", ".<br/>NOTE:")?trim} </#list>
				</div>
				</#if>
				
				<#if subreq.showExcLines>
				<#list subreq.appliedExceptionText as ex>
				<span class="subreqCline">${ex?xml}</span>
				</#list>
				</#if>
				
				<#if subreq.showGotSummary || subreq.showInProgressHours || subreq.showNeedsSummary >
				<div class="totals">
					<#if subreq.showGotSummary>
		            <span class="earned">
		                Earned:
		            	<#if subreq.showGotHours>
		        		 ${subreq.gotHoursOpenDecoration} <span class="value"> ${subreq.gotHours?xml} </span> ${subreq.gotHoursText?xml}${subreq.gotHoursCloseDecoration}
						</#if>
		            	<#if subreq.showGotCount>
		            	<span class="value"> ${subreq.gotCount?xml} </span> ${subreq.gotCountText?xml}
		            	</#if>
		                <#if subreq.showGotGpa>
		                <span class="value"> ${subreq.gotGpa?xml} </span> GPA
		                </#if>
		            </span>
		            </#if> 
					<!--
					<#if subreq.showDetailGpaLine>
	                <span class="gpadetail">
	                	${subreq.gotGpaHours?xml}
	                	${subreq.gotGpaHoursText?xml}
	                    ${subreq.gotGpaPoints?xml}
	                    ${subreq.gotGpaPointsText?xml}
	                    ${subreq.gotGpa?xml} GPA
	                </span>
	                </#if>
					-->
					<#if subreq.showInProgressHours >
	                <span class="inprogress">
	                	In-progress:
	                	${subreq.ipSrStub?xml}
	                	<#if subreq.showInProgressHours >
	                	<span class="value"> ${subreq.ipHours?xml} </span> ${subreq.ipHoursText?xml}
	                	</#if>
	                	<#if subreq.showInProgressCount >
	                    <span class="value"> ${subreq.ipCount?xml} </span> ${subreq.ipCountText}
	                	</#if>
	                </span>
					</#if> 
					<!--
					<#if subreq.showPlannedHours > 
	                <span class="subreqWhatIfDetail">
	                    ${subreq.wifSrStub?xml} 
	                    ${subreq.plannedHours?xml}
	                    ${subreq.plannedHoursText?xml}
	                    ${subreq.plannedCount?xml} 
						${subreq.plannedCountText?xml}
	                </span>
	            	</#if>
					-->
					<#if subreq.showNeedsSummary >
					<span class="needs">
						Needs:
						<#if subreq.showNeedsHours >
						<span class="value"> ${subreq.needsHours?xml} </span> ${subreq.needsHoursText?xml} 
						</#if>
						<#if subreq.showNeedsCount >
						<span class="value"> ${subreq.needsCount?xml} </span> ${subreq.needsCountText?xml}
						</#if>
						<#if subreq.showNeedsGpa >
						<span class="value"> ${subreq.needsGpa?xml} </span> GPA
						</#if>
					</span>
					</#if>
	        	</div> <!-- end of totals -->
				</#if>
				
				<#if subreq.showTakenCourses>
				<table class="taken">
					<thead>
						<tr>
							<th>Qtr</th>
							<th colspan="2">Course Name</th>
							<th>Credits</th>
							<th>Grade</th>
							<th></th>
						</tr>
					</thead>
					<tbody>
					<#list subreq.takenCourses as takenCourse>
						<tr class="${takenCourse.courseType}">
							<td class="term">${takenCourse.yt?xml}</td>
							<td class="course">${takenCourse.displayCourse?substring(1,7)?trim?xml} ${takenCourse.displayCourse?substring(7,10)?trim?xml}</td>
							<td class="description"><#list takenCourse.descriptiveLines as descriptiveLine> ${descriptiveLine?xml} </#list></td>
							<td class="credit">${takenCourse.credit?string?replace(".0","")?xml}</td>
							<td class="grade">${takenCourse.grade?xml}</td>
							<td class="ccode" title="${takenCourse.condCode?xml}">${takenCourse.condCode?xml}</td>
						</tr>
					</#list>
					</tbody>
				</table>
				</#if>
				
				<#if subreq.showSelectNotFrom>
				<table class="fromcourses">
					<#if subreq.showReject>
					<tr class="notfromcourses">
						<td class="fromlabel">${subreq.notText?xml}</td>
						<td>
							<table>
		   						<#list subreq.notFromHtmlCourses as course>
								<tr><td class="fromcourselist">${course}</td></tr>
		    					</#list>
		    				</table>
		    			</td>
		    		</tr>
					</#if>
					
					<#if subreq.showAccept>
					<tr class="selectfromcourses">
						<td class="fromlabel">${subreq.selectText?xml}</td>
						<td>
							<table>
		   						<#list subreq.selectFromHtmlCourses as course>
								<tr><td class="fromcourselist">${course}</td></tr>
		    					</#list>
		    				</table>
		    			</td>
		    		</tr>
					</#if>
					
					<tr class="selectfromcourses">
						<td class="fromlabel">${subreq.selectText?xml}</td>
						<td>
							<table>
		   						<#list subreq.selectFromHtmlCourses as course>
								<tr><td class="fromcourselist">${course}</td></tr>
		    					</#list>
		    				</table>
		    			</td>
		    		</tr>
				</table>
				
					<#if subreq.showNoRefCoursesFoundMessage>
						<#rt>${noRefCourseMessage?xml}
					</#if>
				</#if>
			</div> <!-- end of subrequirement -->
			</#list>
		</div> <!-- end of requirement -->
		
	</#if>

</#list>

<#if inSection>
	</div>
</#if> 

<#if showIncludeBottomText>
    <div class="fl-text-align-center" >
    <#list includeBottomText as bottomTextLine>
	${bottomTextLine?replace("*", "")?replace("_", "")?replace("END OF ANALYSIS", "<br/>END OF ANALYSIS")?trim }      
    </#list>
     <hr class="headerRule" />
    </div>
</#if>    
       
</div>
</html>