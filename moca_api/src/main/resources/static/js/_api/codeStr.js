var divSample = 
	'							<div class="col-md-12">\n' + 
	'								{cls.method.sample.desc}\n' + 
	'								<hr>\n' + 
	'								<pre id="sample_response{j}">{cls.method.sample.resp}</pre>\n' + 
	'							</div>';


var inputSandbox =  
	'								<div class="form-group inline">\n' + 
	'									<label style="display:inline;width:50px;">{cls.method.sandbox.name} : </label> <input type="text" class="form-control input-sm" id="{cls.method.sandbox.name}" value="{cls.method.sandbox.value}" name="{cls.method.sandbox.name}" style="display:inline;width:200px;">' +
	'								</div>';
var inputSandbox_file =  
	'								<div class="form-group inline">\n' + 
	'									<label style="display:inline;width:50px;">{cls.method.sandbox.name} : </label> <input type="file" multiple="multiple" class="form-control input-sm" id="{cls.method.sandbox.name}" value="{cls.method.sandbox.value}" name="{cls.method.sandbox.name}" style="display:inline;width:300px;">' +
	'								</div>';	

var reqParam = '								\n' +  
				'<tr><td>{cls.method.info.reqParam.name}</td>\n' + 
				'<td>{cls.method.info.reqParam.type}</td>\n' + 
				'<td>{cls.method.info.reqParam.req}</td>\n' + 
				'<td>{cls.method.info.reqParam.desc}</td></tr>';   

var respParam = '								\n' +  
				'<tr><td>{cls.method.info.respParam.name}</td>\n' + 
				'<td>{cls.method.info.respParam.type}</td>\n' + 
				'<td>{cls.method.info.respParam.desc}</td></tr>';   
    
var divMethodOrg =
	'	<div class="panel panel-default">\n' + 
	'		<div class="panel-heading">\n' + 
	'			<h4 class="panel-title">\n' + 
	'				<span class="label label-{cls.method.label}" style="font-size:14px;">{cls.method.name}</span>&nbsp;&nbsp;&nbsp;<a data-toggle="collapse" data-parent="#accordion{j}" href="#collapseOne{j}"> - {cls.method.desc} </a>\n' + 
	'			</h4>\n' + 
	'		</div>\n' + 
	'		<div id="collapseOne{j}" class="panel-collapse collapse">\n' + 
	'			<div class="panel-body">\n' + 
	'				<!-- Nav tabs -->\n' + 
	'				<ul class="nav nav-tabs" id="php-apidoctab{j}">\n' + 
	'					<li class="active"><a href="#info{j}" data-toggle="tab">Info</a></li>\n' + 
	'					<li><a href="#sandbox{j}" data-toggle="tab">Sandbox</a></li>\n' + 
	'					<li><a href="#sample{j}" data-toggle="tab">Sample output</a></li>\n' + 
	'				</ul>\n' + 
	'				<!-- Tab panes -->\n' + 
	'				<div class="tab-content">\n' + 
	'					<div class="tab-pane active" id="info{j}">\n' + 
	'						{cls.method.desc}<BR>\n' +
	'						- URL : {cls.method.info.url}<BR>\n' + 
	'						{cls.method.usage}<BR>\n' +
	'						<hr>\n' + 
	' 						- <i>Request Parameter</i>\n' +
	'						<table class="table table-hover">'+
	'							<thead>'+
	'								<tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>'+
	'							</thead>'+
	'							<tbody>'+
	'								{reqParam}\n' +
	'							</tbody>'+
	'						</table>'+
	'						<hr>\n' + 
	' 						- <i>Response Parameter</i>\n' +
	'						<table class="table table-hover">'+
	'							<thead>'+
	'								<tr><th>Name</th><th>Type</th><th>Description</th></tr>'+
	'							</thead>'+
	'							<tbody>'+
	'								{respParam}\n' +
	'							</tbody>'+
	'						</table>'+
	'					</div>\n' + 
	'					<div class="tab-pane" id="sandbox{j}">\n' + 
	'						<div class="row">\n' + 
	'							<div class="col-md-4">\n' + 
	'								Parameters\n' + 
	'								<hr>\n' + 
	'								<form enctype="application/x-www-form-urlencoded" role="form" action="{cls.method.info.url}" method="{cls.method.info.method}" name="form{j}" id="form{j}">\n' +
	'									{inputSandbox}\n' +
	'								<button type="submit" class="btn btn-success send" rel="{j}">Send</button>\n' + 
	'								</form>\n' + 
	'							</div>\n' + 
	'							<div class="col-md-8">\n' + 
	'								Response\n' + 
	'								<hr>\n' + 
	'								<pre id="response{j}"></pre>\n' + 
	'							</div>\n' + 
	'						</div>\n' + 
	'					</div>\n' + 
	'					<div class="tab-pane" id="sample{j}">\n' + 
	'						<div class="row">\n' + 
	'							{divSample}\n' +
	'						</div>\n' + 
	'					</div>\n' + 
	'				</div>\n' + 
	'			</div>\n' + 
	'		</div>\n' + 
	'	</div>';

var divMethodUrl =
	'	<div class="panel panel-default">\n' + 
	'		<div class="panel-heading">\n' + 
	'			<h4 class="panel-title">\n' + 
	'				<span class="label label-{cls.method.label}" style="font-size:14px;">{cls.method.name}</span>&nbsp;&nbsp;&nbsp;<a data-toggle="collapse" data-parent="#accordion{j}" href="#collapseOne{j}"> - {cls.method.desc} </a>\n' + 
	'			</h4>\n' + 
	'		</div>\n' + 
	'		<div id="collapseOne{j}" class="panel-collapse collapse">\n' + 
	'			<div class="panel-body">\n' + 
	'				<!-- Nav tabs -->\n' + 
	'				<ul class="nav nav-tabs" id="php-apidoctab{j}">\n' + 
	'					<li class="active"><a href="#info{j}" data-toggle="tab">Info</a></li>\n' + 
	'					<li><a href="#sandbox{j}" data-toggle="tab">Sandbox</a></li>\n' + 
	'					<li><a href="#sample{j}" data-toggle="tab">Sample output</a></li>\n' + 
	'				</ul>\n' + 
	'				<!-- Tab panes -->\n' + 
	'				<div class="tab-content">\n' + 
	'					<div class="tab-pane active" id="info{j}">\n' + 
	'						{cls.method.desc}<BR>\n' +
	'						- URL : {cls.method.info.url}<BR>\n' + 
	'						{cls.method.usage}<BR>\n' +
	'						<hr>\n' + 
	' 						- <i>Request Parameter</i>\n' +
	'						<table class="table table-hover">'+
	'							<thead>'+
	'								<tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>'+
	'							</thead>'+
	'							<tbody>'+
	'								{reqParam}\n' +
	'							</tbody>'+
	'						</table>'+
	'						<hr>\n' + 
	' 						- <i>Response Parameter</i>\n' +
	'						<table class="table table-hover">'+
	'							<thead>'+
	'								<tr><th>Name</th><th>Type</th><th>Description</th></tr>'+
	'							</thead>'+
	'							<tbody>'+
	'								{respParam}\n' +
	'							</tbody>'+
	'						</table>'+
	'					</div>\n' + 
	'					<div class="tab-pane" id="sandbox{j}">\n' + 
	'						<div class="row">\n' + 
	'							<div class="col-md-4">\n' + 
	'								Parameters\n' + 
	'								<hr>\n' + 
	'		<div class="form-group inline"><label style="display:inline;width:50px;">URL : </label> <input type="text" class="form-control input-sm" id="_url_org_" value="{cls.method.info.url}" name="url" style="display:inline;width:200px;"></div>' +
	'								<form enctype="application/x-www-form-urlencoded" role="form" action="{cls.method.info.url}" method="post" name="form{j}" id="form{j}">\n' +
	'									{inputSandbox}\n' +
	'								<button type="submit" class="btn btn-success send" rel="{j}">Send</button>\n' + 
	'								</form>\n' + 
	'							</div>\n' + 
	'							<div class="col-md-8">\n' + 
	'								Response\n' + 
	'								<hr>\n' + 
	'								<pre id="response{j}"></pre>\n' + 
	'							</div>\n' + 
	'						</div>\n' + 
	'					</div>\n' + 
	'					<div class="tab-pane" id="sample{j}">\n' + 
	'						<div class="row">\n' + 
	'							{divSample}\n' +
	'						</div>\n' + 
	'					</div>\n' + 
	'				</div>\n' + 
	'			</div>\n' + 
	'		</div>\n' + 
	'	</div>';


var divClass =
	'  <div class="div-header">\n' +
	'	<h2>{cls.name} : {cls.desc}</h2>\n' + 
	'	<button class="toggle"></button>\n' +
	'  </div>\n' +
	'	{divMethod}\n';
	
	
	
var progClass =
	'	<td>{prog.no}</td>' + 
	'	<td>{prog.1depth}</td>' + 
	'	<td>{prog.2depth}</td>' + 
	'	<td>{prog.title}</td>' + 
	'	<td>{prog.rate}</td>' + 
	'	<td>{prog.desc}</td>';

var statusClass =
	'	<td>{prog.no}</td>' + 
	'	<td>{prog.1depth}</td>' + 
	'	<td>{prog.2depth}</td>' + 
	'	<td>{prog.title}</td>' + 
	'	<td>{prog.rate}</td>';
