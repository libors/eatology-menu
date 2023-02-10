<html>
<head>
    <link rel="stylesheet" type="text/css" href="${cssPath}">
    <script src="https://kit.fontawesome.com/36e07d234f.js" crossorigin="anonymous"></script>

    <#if admin>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
        <script type="application/javascript">
            $(document).ready(function() {
                $('.setflag').click(function() {
                    if ($(this).hasClass("off")) {
                        $(this).removeClass("off");
                    } else {
                        $(this).addClass("off");
                    }
                });
                $('#savebutton').click(function() {
                   var data = {};
                   $(".setflag").each(function() {
                       var item = $(this);
                       data[item.attr('id')] = !item.hasClass("off");
                   });
                    $.ajax({
                        "url": "/iqadmin/update",
                        "dataType": "text",
                        "type": "post",
                        "contentType": "application/json",
                        "processData": false,
                        "data": JSON.stringify({"day": "${menu.name}", "flags": data}),
                        success: function(){$('#saveresponse').text("saved");},
                        error: function(){$('#saveresponse').text("error");},
                        beforeSend: function (xhr) {
                            xhr.setRequestHeader ("Authorization", "Basic " + btoa("admin:${password}"));
                        },
                    });

                });
                $('#regeneratebutton').click(function() {
                      $.ajax({
                        "url": "/iqadmin/regenerate/${menu.name}",
                        "type": "put",
                        success: function(){location.reload();},
                        error: function(){$('#saveresponse').text("error");},
                        beforeSend: function (xhr) {
                            xhr.setRequestHeader ("Authorization", "Basic " + btoa("admin:${password}"));
                        },
                    });

                });
            });
        </script>
    </#if>
</head>

<body>

<#macro pictures flags>
    <span <#if flags?seq_contains("ai")>style="filter:blur(1px)"</#if>)>
    <#if flags?seq_contains("vege")><i title="vege" class="fas fa-leaf vege" style="color:green"></i></#if>
    <#if flags?seq_contains("hot")><i title="pálivé" class="fas fa-pepper-hot hot" style="color:red"></i></#if>
    <#if flags?seq_contains("glut")><i title="bez lepku" class="fas fa-thumbs-up glut" style="color:gray"></i></#if>
    </span>
</#macro>

<#macro settings number flags>
    <#if admin>
    <span id="settings-" style="background-color: bisque">
        <i id="vege-${number}" class="setflag fas fa-leaf ${flags?seq_contains('vege')?string('', 'off')}" style="color:green"></i>
        <i id="hot-${number}" class="setflag fas fa-pepper-hot ${flags?seq_contains('hot')?string('', 'off')}" style="color:red"></i>
        <i id="glut-${number}" class="setflag fas fa-thumbs-up ${flags?seq_contains('glut')?string('', 'off')}" style="color:gray"></i>
    </span>
    </#if>
</#macro>

<#macro meals mealList>
    <#list mealList as catName, category>
        <div class="category">${catName}</div>
        <#list category as meal>
            <div class="meal">
                <@settings count meal.flags![]/>
                <#assign count = count + 1>
                ${meal.name} <span class="price">(${meal.price})</span>
                <#if meal.flags?has_content><@pictures meal.flags/></#if>
            </div>
        </#list>
    </#list>
</#macro>

<#assign count = 0>
<div class="title">${dayName}, ${menu.name}</div>
<div class="subtitle">Denní menu</div>
<@meals menu.daily />
<div class="subtitle">Týdenní menu</div>
<@meals menu.weekly />

<#if admin>
    <button id="savebutton" type="button">Save changes</button>
    <button id="regeneratebutton" type="button">Regenerate</button>
    <div id="saveresponse"></div>
</#if>

<div class="links">
    <a href="/${admin?string('iqadmin', 'iq')}/${menu.name}-prev">&lt;&lt; Předchozí</a>
    <a href="/${admin?string('iqadmin', 'iq')}/${menu.name}-next">Následující &gt;&gt;</a>
</div>

</body>
</html>