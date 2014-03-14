<#--

    Copyright 2005-2013 The Kuali Foundation

    Licensed under the Educational Community License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.opensource.org/licenses/ecl2.php

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<#--
    Element that creates a calendar element and then invokes fullcalendar to complete
    the calendar based on configured source data
 -->
<#macro ksap_slider element>

<div id="${element.id!}_wrapper" class="sliderWrapper">
    <div id="${element.id!}_label" class="sliderLabel"></div>
    <@krad.div component=element />
</div>

<@krad.script value="createSlider('${element.id}', ${element.templateOptionsJSString}); "/>

</#macro>
