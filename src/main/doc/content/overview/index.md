+++
date = "2016-04-21T16:03:16+02:00"
draft = true
title = "Overview"

+++

Ever desperately tried to get a RESTful API up with JAX-RS? Annotations, XML configs, service definitions piling up in your project? Enviously gazing at your colleagues using Node.js or Golang for their one liners?

Well, <b>Cargo</b> is for you who just don't build but actually ship something. Java is a great ecosystem with lots of toolchains and frameworks. It is battle tested as a weathered sea dog and entertains the biggest community according to the <a href="http://www.tiobe.com/tiobe_index">TIOBE index</a> (as of April 2016).

Why not get rid off the overhead and start getting things done? Here's a sample RESTful API:

	
	package io.datenwelt.cargo.rest.examples;
	
	import io.datenwelt.cargo.rest.Endpoint;
	import io.datenwelt.cargo.rest.Router;
	import io.datenwelt.cargo.rest.response.OK;
	import javax.servlet.ServletException;
	
	public class HelloAPI extends Router {
	    
	    private static final Endpoint HELLO = (req) -> {
	        return new OK("Hello World!");
	    };
	    
	    @Override
	    public void init() throws ServletException {
	        GET("/say/hello", HELLO);
	    }
	
	}

OK, what's happening here? We just created an API which exposes one resource via `GET http://somewhere.org/say/hello`.	

