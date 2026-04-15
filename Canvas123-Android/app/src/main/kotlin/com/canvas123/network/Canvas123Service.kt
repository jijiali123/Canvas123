package com.canvas123.network

interface Canvas123Service {
    // Define API endpoints for the Cloudflare Worker service
    @GET("/example-endpoint")
    suspend fun getExampleData(): Response<ExampleData>

    @POST("/submit-data")
    suspend fun submitData(@Body data: SubmissionData): Response<ResponseBody>
}