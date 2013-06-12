<html>
<#-- using svn's keyword substition -->
<#assign svnRev    = "$Rev::                      $:  Revision of last commit" >
<#assign svnAuthor = "$Author::                   $:  Author of last commit">
<#assign svnDate   = "$Date::                     $:  Date of last commit">

<#-- list of rnames to skip (not show) -->
<#assign skipList = [
"LINE1WHAT", "LINE1"
]>

<#assign sectionHeadingOpen = false>

<#assign toolTipsMap = {
"+":"The sub-requirement has been satisfied.",
"-":"The sub-requirement has not been satisfied.",
"IP -":"A course is in-progress which partially satisfies the sub-requirement but does not complete it.",
"IP +":"A course is in-progress which partially satisfies the sub-requirement.",
"0000":"Zeros precede some transfer courses to indicate that quarter and year the course was taken are not available to DARS.",
"R":"This sub-requirement is mandatory.",
"*":"This sub-requirement is optional.",
">S":"The course credit is split.",
">R":"The course is repeatable.",
">-":"The course has exceeded the repeatable limit and has had its credit reduced.",
"DP":"This course has been retaken.",
">D":"Credit has been removed from this retaken course. For the purpose of a given requirement, credit may be restored--as when a minimum grade is required. This course is used in your UW GPA.",
">PL":"Planned Course"
}>

<#assign subreqStatusMap = {
"Status_NONE":"",
"Status_NO":"-",
"Status_OK":"+",
"Status_IP":"+",
"Status_PL":"+"
}>

<#assign termMap = {
"1":"WIN",
"2":"SPR",
"3":"SUM",
"4":"AUT"
}>

<#assign satisfiedMap = {
"Status_NONE" : "",
"Status_NO" : "NO",
"Status_IP" : "IP",
"Status_OK" : "OK",
"Status_PL" : "PL"
}>
<#macro myplanstatus>
<div class="myplan-status info uif-boxLayoutVerticalItem all-reqs-filtered"
     style="margin-bottom:20px; float:none; display:none;">
    <img src="/student/ks-myplan/images/pixel.gif" alt="" class="icon"/>

    <div class="message">
        All requirements in this section have been hidden. See &quot;All Requirements&quot; for
        the full audit report.
    </div>
</div>
</#macro>

<head>
    <link href="https://uwksdev01.cac.washington.edu/student/ks-myplan/css/audit.css" rel="stylesheet" type="text/css"/>
</head>
<div class="myplan-audit-report" dprog="${degreeProgramCode?html}" auditid="0">
    <h1> ${dpTitle1?xml} </h1>

    <div class="audit-summary">
        <div class="audit-summary-data plan-audit-data">
            <label>Planned Through:</label>

            <div>
                <span class="for-quarter"> FOR-QUARTER </span>
            </div>
        </div>
        <div class="audit-summary-data ">
            <label>Date Prepared:</label>

            <div>
                <span class="date-prepared"> ${preparedDate} </span>
            </div>
        </div>
        <div class="audit-summary-data plan-audit-data">
            <label>Planned Courses:</label>

            <div>
                <span class="for-courses"> FOR-COURSES </span> courses <span class="ksap-text-gray">(<span
                    class="for-credits"> FOR-CREDITS </span> credits)</span>
            </div>
        </div>
        <div class="audit-summary-data">
            <label>Program Entry Qtr:</label>

            <div>
                <span class="program-entry-qtr"> ${termMap[catalogYearTerm?substring(4,5)]} ${catalogYearTerm?substring(0,4)} </span>
            </div>
        </div>
        <div class="audit-summary-data plan-audit-data">
            <label>Requested By:</label>

            <div>
                <span class="prepared-by"> PREPARED-BY </span>
            </div>
        </div>
        <div class="audit-summary-data">
        <#assign stuno = "${studentNumber}">
        <#if ( stuno?starts_with( "1" ) && stuno?length == 9 )>
            <#assign stuno = stuno?substring(1)>
        </#if>
            <label>Prepared For:</label>

            <div>
                <span class="prepared-for-name" stuno="${stuno}"> ${stuno} </span>
            </div>
        </div>

    <#if degreeDate?trim != "NotFound">
        <div class="audit-summary-data ">
            <label>Graduation Date:</label>

            <div>
                <span class="graduation-date"> ${degreeDate?replace("/", " ")} </span>
            </div>
        </div>
    </#if>
    </div>

<#--
<#if showTestMessage>
<div id="testMessageHeader">
    ${testMessage?xml}
</div>
</#if>
-->

    <div class="toptext">
    <#list includeTopText as topTextLine>
        ${ deASCII( topTextLine )?trim?xml}
 		</#list>
    </div>

<#--
<#if showRefArtHeader>
<div id="refArtHeaderText">
    ${refArtHeaderTextLine1?xml}
    ${refArtHeaderTextLine2?xml}
</div>
</#if>
-->

<#if auditStatus = 0>
    <div class="audit-status-msg audit-status-ok">
        <label>NOTE:</label> ${auditStatusMessage?replace( "*", "")?xml}
    </div>
<#elseif auditStatus = -1>
    <div class="audit-status-msg audit-status-no">
        <label>NOTE:</label> ${auditStatusMessage?replace( "NOTE:", "")?xml}
    </div>
<#else>
    <div class="audit-status-msg audit-status-ip">
        <label>NOTE:</label> ${auditStatusMessage?replace( "NOTE:", "")?xml}
    </div>
</#if>

<#--
    cncflg: ${cncflg?string?xml}
    cmess: ${cmess?xml}
    ncmess: ${ncmess?xml}
-->


<#assign inSection = false>
<#assign needsDropdown = true>

<#list auditReportReqs as req>

    <#assign rname = req.rname?xml?substring(7)?trim>
    <#assign satisfied = "${req.satisfied?xml}" >
    <#if !req.showStatus >
        <#assign satisfied="Status_NONE">
    </#if>
<#--
rname: ${rname?xml}
ok: ${req.ok?string?xml}
category: ${req.category?xml}
status: ${req.status?xml}

titleline:
<#list req.titleLines as titleLine>
${titleLine?xml}
</#list>

reflow titleline:
<#list reflow( req.titleLines ) as titleLine>
${titleLine?xml}
</#list>

headerline:
<#list req.headerLines as headerLine>
${headerLine?xml}
</#list>

reflow headerline:
<#list reflow( req.headerLines ) as headerLine>
${headerLine?xml}
</#list>

-->

    <#if skipList?seq_contains(rname) >
    <#-- do nothing -->
    <#elseif req.category?contains("advising_note") > <#-- ADVISING NOTES -->

        <#if sectionHeadingOpen = true>
        </div>
            <@myplanstatus/>
            <#assign sectionHeadingOpen = false>
            <#assign inSection = true>
        </#if>

        <#if inSection>
        </div> <#-- close section -->
            <#assign inSection = false>
        </#if>

    <div class="advisory ${rname} ${satisfied}">
        <#list reflow(req.headerLines) as headerLine>
            <div class="text linkify">
            ${deASCII(headerLine)}
            </div>
        </#list>
    </div>
    <#elseif req.category?contains("other_courses" )  ><#-- OTHER COURSES aka hungry -->

        <#if sectionHeadingOpen = true>
        </div>
            <@myplanstatus/>
            <#assign sectionHeadingOpen = false>
            <#assign inSection = true>
        </#if>

        <#if inSection>
        </div> <#-- close section -->
            <#assign inSection = false>
        </#if>

    <div class="section ${rname}">
        <div class="heading">
            <#list req.titleLines as titleLine> ${deASCII( titleLine?trim )} </#list>
        </div>
        <@myplanstatus/>

        <#list req.auditReportSubreqs as subreq>
            <div class="requirement">
                <div class="header">
                    <div class="toggle"></div>
                    <div class="status Status_NONE"></div>
                    <#if subreq.showTitle >
                        <div class="title">
                            <#list reflow( subreq.titleLines ) as titleLine>
                                <div class="text linkify">
                                ${deASCII(titleLine)}
                                </div>
                            </#list>
                        </div>
                    </#if>
                </div>
                <div class="body">
                    <#if subreq.showTakenCourses>
                        <table class="taken">
                            <thead>
                            <tr>
                                <th> Qtr</th>
                                <th colspan="2"> Course Name</th>
                                <th> Credits</th>
                                <th> Grade</th>
                                <th></th>
                            </tr>
                            </thead>
                            <tbody>
                                <#list subreq.takenCourses as takenCourse>
                                <tr class="${takenCourse.courseType}">
                                    <td class="term"> ${takenCourse.yt?xml} </td>
                                    <td class="course linkify"> ${takenCourse.displayCourse?substring(1,7)?trim?xml} ${takenCourse.displayCourse?substring(7,10)?trim?xml} </td>
                                    <td class="description">
                                        <#list reflow(takenCourse.descriptiveLines) as line>
                                        ${line?xml} <#if line_has_next > <br/> </#if>
                                        </#list>
                                    </td>
                                    <td class="credit"> ${takenCourse.credit?string?replace(".0","")?xml} </td>
                                    <td class="grade"> ${takenCourse.grade?xml} </td>
                                    <#assign condCode = takenCourse.condCode?trim >
                                    <#if toolTipsMap[condCode]?exists >
                                        <td class="ccode" title="${toolTipsMap[condCode]}">
                                        ${condCode?xml}
                                        </td>
                                    <#else>
                                        <td class="ccode"></td>
                                    </#if>
                                </tr>
                                </#list>
                            </tbody>
                        </table>
                    </#if>
                </div>
            </div>
        </#list>
    </div>
    <#elseif ( req.headerLines?size > 2 )  > <#-- temporary fix for overly large headers, treat it as a requirement -->
        <#if sectionHeadingOpen = true>
        </div>
            <@myplanstatus/>
            <#assign sectionHeadingOpen = false>
            <#assign inSection = true>
        </#if>

        <#if inSection>
        </div> <#-- close section -->
            <#assign inSection = false>
        </#if>

    <div class="bigsection bignote ${rname} ${satisfied}">
        <div class="heading">
            <#list reflow(req.headerLines) as headerLine>
                <div class="text linkify">
                ${deASCII(headerLine)}
                </div>
            </#list>
        </div>
        <@myplanstatus/>
        <#assign inSection = true>

    <#elseif ( req.headerLines?size > 0 )  > <#-- SECTION -->

        <#if inSection>
        </div> <#-- close section -->
            <#assign inSection = false>
        </#if>

        <#if sectionHeadingOpen = false>
        <div class="section ${rname} ${satisfied}">
            <#if needsDropdown >
                <#assign needsDropdown = false>
            <div class="control-toolbar">
                <label for="requirement-status"> Show </label>
                <select id="requirement-status">
                    <option value="all">All Requirements</option>
                    <option value="unmet">Unmet Requirements Only</option>
                </select>
            </div>
            <div class="myplan-status alert uif-boxLayoutVerticalItem audit-filtered"
                 style="margin-bottom:20px; float:none; display:none;">
                <img src="/student/ks-myplan/images/pixel.gif" alt="" class="icon"/>

                <div class="message">You are viewing a partial degree audit report. See &quot;All Requirements&quot; for
                    the full audit report.
                </div>
            </div>
            </#if>

        <div class="heading">
        </#if>

        <#if sectionHeadingOpen = true> <br/> </#if>
        <#list reflow(req.headerLines) as headerLine>
            <div class="text linkify">
            ${deASCII(headerLine)}
            </div>
        </#list>
    <#--
    <#list req.headerLines as headerLine>
    ${headerLine?replace("*", "")?replace("_[_]+", " ", "r")?replace(". NOTE:", ".<br/>NOTE:")?trim}
    <#if headerLine_has_next><br /> </#if>
    </#list>
    -->
        <#assign sectionHeadingOpen = true>

    <#else> <#-- REQUIREMENT -->

        <#if sectionHeadingOpen = true>
        </div>
            <@myplanstatus/>
            <#assign sectionHeadingOpen = false>
            <#assign inSection = true>

        </#if>

        <#if inSection = false>
        <div class="section"> <#-- no section -->
        </#if>

        <#assign inSection = true>

        <#if needsDropdown ><#-- cut and paste from above, sorry! -->
            <#assign needsDropdown = false>
        <div class="control-toolbar">
            <label for="requirement-status">Show</label>
            <select id="requirement-status">
                <option value="all">All Requirements</option>
                <option value="unmet">Unmet Requirements Only</option>
            </select>
        </div>
        </#if>
    <div class="requirement ${rname} ${satisfied} ${req.summaryGroupName?xml}">
    <div class="header">
        <div class="toggle"></div>
        <div class="status ${satisfied}"> ${satisfiedMap[satisfied]} </div>
        <#if req.showNumber>
            <div class="reqNumber"> ${req.number?xml} </div></#if>
        <#if req.showGroups>
            <div class="reqGroups"> ${req.groups?xml} </div></#if>
        <div class="title">
            <#list reflow( req.titleLines ) as titleLine>
                <div class="text linkify">
                ${deASCII(titleLine)}
                </div>
            </#list>
        </div>
    </div>

    <div class="body">
        <#if req.showGotSummary || req.showInProgressHours || req.showPlannedHours || req.showNeedsSummary >
        <div class="totals">
            <#if req.showGotSummary>
                <span class="earned">
		                <#if req.showWarnInd>${req.warnInd?xml}</#if>
                    Earned:
                    <#if req.showGotHours>
                        <span class="value">${req.gotHours?replace(".0","")?xml}</span> ${req.gotHoursText?xml}
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
        <#--
        <#if req.showDetailGpaLine>
        <span class="reqGpaDetail">
            ${req.gotGpaHours}
            ${req.gotGpaHoursText?xml}
            ${req.gotGpaPoints}
            ${req.gotGpaPointsText?xml}
            ${req.gotGpa} GPA
        </div>
        </#if>
        -->
            <#if req.showInProgressHours>
                <span class="inprogress">
		            	In-progress: 
		            	<span class="value"> ${req.ipHours?replace(".0","")?xml} </span> ${req.ipHoursText?xml} 
		            </span>
            </#if>

            <#if req.showPlannedHours>
                <div class="whatif">
                    Planned:
                <#--  ${req.wifStub?xml}   -->
                    <span class="value">${req.wifHours?replace(".0","")?xml} </span> ${req.wifHoursText?xml}
                </div>
            </#if>

            <#if req.showNeedsSummary>
                <span class="needs">
		                Needs:
                    <#if req.showNeedsHours>
                        <span class="value"> ${req.needsHours?replace(".0","")?xml} </span> ${req.needsHoursText?xml}
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
        </div> <#-- end of totals -->
        </#if>

        <#if req.showExcLines>
        <div class="exceptions">
            <#list req.appliedExceptionText as ex>
                <div class="reqCline"> ${ex?xml} </div></#list>
        </div>
        </#if>

        <#list req.auditReportSubreqs as subreq>
        <#-- Have to grab flag -->
            <#assign showSubreqStatus = subreq.showSubreqStatus >
            <#assign justTitle = "">
            <#if ( !subreq.showGotSummary && !subreq.showInProgressHours && !subreq.showNeedsSummary && !subreq.showTakenCourses && !subreq.showSubreqNumber && subreq.status == "Status_NONE" ) >
                <#assign justTitle = "justtitle" >
            </#if>
        <!--
                showExcLines: ${subreq.showExcLines?string?xml}

                showSubreqStatus: ${showSubreqStatus?string?xml}

                <#-- Because calling this method resets it to false -->
                subreqStatus: ${subreq.subreqStatus?string?xml}
                status: ${subreq.status?xml}
                satisfiedCode: ${subreq.satisfiedCode?string?xml}
                statusClassSR: ${subreq.statusClassSR?string?xml}
                seqErr: ${subreq.seqErr?xml}

                showTitle: ${subreq.showTitle?string?xml}
                required: ${subreq.required?string?xml}
                subreqRequired: ${subreq.subreqRequired?xml}
                showSubreqNumber: ${subreq.showSubreqNumber?string?xml}
                subreqNumber: ${subreq.subreqNumber?xml}
                subreq titleLines:
                    <#list subreq.titleLines as titleLine>
                        <#if titleLine?trim == "." > just period </#if>
                        <#if titleLine?trim != "." > not period </#if>
                    ${deASCII(titleLine)?xml}
                    </#list>
                subreq reflow titleLines:
                    <#list reflow( subreq.titleLines ) as titleLine>
                        <#if titleLine?trim == "." > just period </#if>
                        <#if titleLine?trim != "." > not period </#if>
                    ${deASCII(titleLine)?xml}
                    </#list>
                justTitle: ${justTitle?xml}


            -->

            <#assign showSubreqStatusX = showSubreqStatus && !( subreq.status == "Status_NONE" && subreq.seqErr?trim == "" ) >
            <#assign showExcLines = ( subreq.showExcLines && subreq.appliedExceptionText?size > 0 ) >
            <#assign showHeader = showSubreqStatusX || subreq.required || subreq.showSubreqNumber || subreq.showTitle || showExcLines >
            <#assign showTotals = subreq.showGotSummary || subreq.showInProgressHours || subreq.showPlannedHours || subreq.showNeedsSummary >

            <#if showHeader || showTotals || subreq.showSelectNotFrom >
            <div class="subrequirement ${justTitle}">
            <div class="header">
                <#if showSubreqStatus >
                    <div class="status ${subreq.status?xml}"> ${subreq.seqErr?xml}${subreqStatusMap[subreq.status]} </div>
                </#if>

                <div class="subreqNumber required">
                    <#if subreq.required>${subreq.subreqRequired?xml}</#if>
                    <#if subreq.showSubreqNumber>${subreq.subreqNumber?xml}
                        <#if subreq.showParen>)</#if>
                    </#if>
                </div>
                <#if subreq.showTitle >
                    <div class="title">
                        <#list reflow( subreq.titleLines ) as titleLine>
                            <#if titleLine?trim != "." >
                                <div class="text linkify">
                                ${deASCII(titleLine)}
                                </div>
                            </#if>
                        </#list>
                    </div>
                </#if>

                <#if showExcLines >
                    <#list subreq.appliedExceptionText as ex>
                        <div class="subreqCline"> ${ex?xml} </div>
                    </#list>
                </#if>
            </div>

                <#if showTotals >
                <div class="totals">
                    <#if subreq.showGotSummary>
                        <span class="earned">
			                Earned:
                            <#if subreq.showGotHours>
                            ${subreq.gotHoursOpenDecoration} <span
                                    class="value"> ${subreq.gotHours?replace(".0","")?xml} </span> ${subreq.gotHoursText?xml}${subreq.gotHoursCloseDecoration}
                            </#if>
                            <#if subreq.showGotCount>
                                <span class="value"> ${subreq.gotCount?xml} </span> ${subreq.gotCountText?xml}
                            </#if>
                            <#if subreq.showGotGpa>
                                <span class="value"> ${subreq.gotGpa?xml} </span> GPA
                            </#if>
			            </span>
                    </#if>
                <#--
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
                                <span class="value"> ${subreq.ipHours?replace(".0","")?xml} </span> ${subreq.ipHoursText?xml}
                            </#if>
                            <#if subreq.showInProgressCount >
                                <span class="value"> ${subreq.ipCount?xml} </span> ${subreq.ipCountText}
                            </#if>
		                </span>
                    </#if>

                    <#if subreq.showPlannedHours >
                        <span class="subreqWhatIfDetail">
						Planned:
                        <#--    ${subreq.wifSrStub?xml} -->
                        ${subreq.plannedHours?xml}
                        ${subreq.plannedHoursText?xml}
                        ${subreq.plannedCount?xml}
                        ${subreq.plannedCountText?xml}
		                </span>
                    </#if>

                    <#if subreq.showNeedsSummary >
                        <span class="needs">
							Needs:
                            <#if subreq.showNeedsHours >
                                <span class="value"> ${subreq.needsHours?replace(".0","")?xml} </span> ${subreq.needsHoursText?xml}
                            </#if>
                            <#if subreq.showNeedsCount >
                                <span class="value"> ${subreq.needsCount?xml} </span> ${subreq.needsCountText?xml}
                            </#if>
                            <#if subreq.showNeedsGpa >
                                <span class="value"> ${subreq.needsGpa?xml} </span> GPA
                            </#if>
						</span>
                    </#if>
                </div> <#-- end of totals -->
                </#if>

                <#if subreq.showTakenCourses>
                <table class="taken">
                    <thead>
                    <tr>
                        <th> Qtr</th>
                        <th colspan="2"> Course Name</th>
                        <th> Credits</th>
                        <th> Grade</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list subreq.takenCourses as takenCourse>
                        <tr class="${takenCourse.courseType}">
                            <td class="term"> ${takenCourse.yt?xml} </td>
                            <td class="course linkify"> ${takenCourse.displayCourse?substring(1,7)?trim?xml} ${takenCourse.displayCourse?substring(7,10)?trim?xml} </td>
                            <td class="description"><#list takenCourse.descriptiveLines as descriptiveLine> ${descriptiveLine?xml} </#list></td>
                            <td class="credit"> ${takenCourse.credit?string?replace(".0","")?xml} </td>
                            <td class="grade"> ${takenCourse.grade?xml} </td>
                            <#if toolTipsMap[takenCourse.condCode]?exists >
                                <td class="ccode"
                                    title="${toolTipsMap[takenCourse.condCode]}"> ${takenCourse.condCode?xml} </td>
                            <#else>
                                <td class="ccode"></td>
                            </#if>
                        </tr>
                        </#list>
                    </tbody>
                </table>
                </#if>
            <#--

            subreq.notText: ${subreq.notText?xml}
            <#list subreq.notFromHtmlCourses as course>
            ${course?xml}
            </#list>

            subreq.selectText: ${subreq.selectText?xml}
            <#list subreq.selectFromHtmlCourses as course>
            ${course?xml}
            </#list>

            -->
                <#if subreq.showSelectNotFrom>
                <div class="fromcourses">

                    <#if subreq.showReject>
                        <div class="notfromcourses">
                            <label class="fromlabel"> ${subreq.notText?xml} </label>

                            <div class="fromcourselist linkify flatten">
                                <#list subreq.notFromHtmlCourses as course>
                                    ${course?replace( "&", "&amp;")}
                                    </#list>
                            </div>
                        </div>
                    </#if>

                    <#if subreq.showAccept>
                        <div class="selectfromcourses">
                            <label class="fromlabel"> ${subreq.selectText?xml} </label>

                            <div class="fromcourselist linkify flatten">
                                <#list subreq.selectFromHtmlCourses as course>
                                    ${course?replace( "&", "&amp;")}
                                    </#list>
                            </div>
                        </div>
                    </#if>
                </div>
                <#--
                <#if subreq.showAccept>
                    show accept:
                    <#list subreq.acceptListElements as accept>
                    ${accept.useCourse?string}
                    ${accept.course}
                    ${accept.useFullCourse?string}
                    ${accept.shortCourse}
                    ${accept.courseLink}
                    followedByANumber: ${accept.followedByANumber?string}
                    followingANumber: ${accept.followingANumber?string}
                    padDept: ${accept.padDept?string}
                    dept: ${accept.dept}

                    hasRefCourse: ${accept.hasRefCourse?string}
                    refCourse: ${accept.refCourse}

                    noteElement: ${accept.noteElement?string}
                    note: ${accept.note}

                    symbolElement: ${accept.symbolElement?string}
                    connector: ${accept.connector}

                    spanCourse: ${accept.spanCourse}

                    forceNewLine: ${accept.forceNewLine?string}
                    </#list>
                </#if>
                -->

                    <#if subreq.showNoRefCoursesFoundMessage>
                    ${noRefCourseMessage?xml}
                    </#if>
                </#if>
            </div> <#-- end of subrequirement -->
            </#if>
        </#list>
    </div>
    </div> <#-- end of requirement -->

    </#if>

</#list> <#-- AuditRunReq list -->

<#if inSection>
</div>
</#if>

<#if showIncludeBottomText>
<div class="fl-text-align-center">
    <#list includeBottomText as bottomTextLine>
    ${deASCII(bottomTextLine)?xml?replace("END OF ANALYSIS", "<br/>END OF ANALYSIS")?trim }
    </#list>
    <hr class="headerRule"/>
</div>
</#if>
<input name="script" type="hidden" value="jQuery.publish('NEW_AUDIT');"/>

<div> (audit template updated: ${svnDate?substring( 8, 27 )})</div>
</div>
</html>

<#-- 
Input list of strings, output reflowed list of strings. Most strings are joined, some are broken up.

The order of these tests are significant. Do not change lightly.
-->
<#function reflow sources>
    <#assign targets = []>
    <#assign target = "">
    <#list sources as source >
        <#assign trimmed = source?trim>

        <#if ( trimmed?starts_with( "*" ) && trimmed?ends_with( "*" )) >
            <#assign targets = addTarget( targets, target ) >
            <#assign targets = addTarget( targets, trimmed ) >
            <#assign target = "" >

        <#elseif trimmed?contains( "IMPORTANT NOTE" ) >
            <#assign nth = trimmed?index_of( "IMPORTANT NOTE" ) >
            <#assign temp = trimmed?substring( 0, nth )?trim >
            <#assign target = target + " " + temp >
            <#assign targets = addTarget( targets, target ) >
            <#assign target = trimmed?substring( nth )?trim >

        <#elseif trimmed?contains( "NOTE" ) >
            <#assign nth = trimmed?index_of( "NOTE" ) >
            <#assign temp = trimmed?substring( 0, nth )?trim >
            <#assign target = target + " " + temp >
            <#assign targets = addTarget( targets, target ) >
            <#assign target = trimmed?substring( nth )?trim >

        <#elseif trimmed?contains( "EQUAL TO:" ) >
            <#assign nth = trimmed?index_of( "EQUAL TO:" ) >
            <#assign temp = trimmed?substring( 0, nth )?trim >
            <#assign target = target + " " + temp >
            <#assign targets = addTarget( targets, target ) >
            <#assign target = trimmed?substring( nth )?trim >

        <#elseif trimmed?contains( ">>MATCHED AS:" ) >
            <#assign nth = trimmed?index_of( ">>MATCHED AS:" ) >
            <#assign temp = trimmed?substring( 0, nth )?trim >
            <#assign target = target + " " + temp >
            <#assign targets = addTarget( targets, target ) >
            <#assign target = trimmed?substring( nth )?trim >

        <#elseif ( trimmed == "." ) >
        <#-- do nothing -->

        <#elseif source?starts_with( " " ) >
            <#assign targets = addTarget( targets, target ) >
            <#assign target = trimmed >

        <#else>
            <#assign target = target + " " + trimmed >

        </#if>
    </#list>
    <#assign targets = addTarget( targets, target ) >
    <#return targets>

</#function>

<#-- Only add non-null items to list, prevents empty divs later -->
<#function addTarget targets item>
    <#if ( item?trim?length > 0 ) >
        <#return targets + [item?trim] >
    </#if>
    <#return targets>
</#function>


<#-- 
Removes ASCII art from text 
-->
<#function deASCII text>
    <#return text?replace("*", " ")?replace("_[_]+"," ", "r")?replace("-&gt;","--")?replace("-[-]+", " ", "r")?trim >
</#function>