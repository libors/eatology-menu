<html>
<head>
    <link rel="stylesheet" type="text/css" href="/iq/static/style.css">
</head>

<body>

<div class="title">${dayName}, ${menu.name}</div>
<div class="subtitle">Denní menu</div>
<#list menu.daily as catName, category>
    <div class="category">${catName}</div>
    <#list category as meal>
        <div class="meal">${meal.name} <span class="price">(${meal.price})</span></div>
    </#list>
</#list>
<div class="subtitle">Týdenní menu</div>
<#list menu.weekly as catName, category>
    <div class="category">${catName}</div>
    <#list category as meal>
        <div class="meal">${meal.name} <span class="price">(${meal.price})</span></div>
    </#list>
</#list>

<div class="links">
    <a href="/iq/${menu.name}-prev">&lt;&lt; Předchozí</a>
    <a href="/iq/${menu.name}-next">Následující &gt;&gt;</a>
</div>

</body>
</html>