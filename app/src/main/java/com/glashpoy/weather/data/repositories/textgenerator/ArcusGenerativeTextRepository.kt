package com.glashpoy.weather.data.repositories.textgenerator

import com.glashpoy.weather.data.getBodyOrThrowException
import com.glashpoy.weather.data.local.textgeneration.GeneratedTextCacheDatabaseDao
import com.glashpoy.weather.data.local.textgeneration.GeneratedTextForLocationEntity
import com.glashpoy.weather.data.remote.languagemodel.TextGeneratorClient
import com.glashpoy.weather.data.remote.languagemodel.models.MessageDTO
import com.glashpoy.weather.data.remote.languagemodel.models.TextGenerationPromptBody
import com.glashpoy.weather.di.GeminiClient
import com.glashpoy.weather.domain.models.weather.CurrentWeatherDetails
import kotlinx.coroutines.CancellationException
import javax.inject.Inject


class ArcusGenerativeTextRepository @Inject constructor(
    @GeminiClient private val textGeneratorClient: TextGeneratorClient,
    private val generatedTextCacheDatabaseDao: GeneratedTextCacheDatabaseDao,
) : GenerativeTextRepository {

    override suspend fun generateTextForWeatherDetails(weatherDetails: CurrentWeatherDetails): Result<String> {
        val generatedTextEntity =
            generatedTextCacheDatabaseDao.getSavedGeneratedTextForDetails(
                nameOfLocation = weatherDetails.nameOfLocation,
                temperature = weatherDetails.temperatureRoundedToInt,
                conciseWeatherDescription = weatherDetails.weatherCondition
            )
        if (generatedTextEntity != null) return Result.success(generatedTextEntity.generatedDescription)
        // prompts
        val systemPrompt = """
            You are a weather reporter. Generate a very short, but whimsical description of the weather,
            based on the given information.
        """.trimIndent()
        val userPrompt = """
            location = ${weatherDetails.nameOfLocation};
            currentTemperature = ${weatherDetails.temperatureRoundedToInt};
            weatherCondition = ${weatherDetails.weatherCondition};
            isNight = ${weatherDetails.isDay != 1}
        """.trimIndent()
        // prompt messages
        val promptMessages = listOf(
            MessageDTO(role = MessageDTO.Roles.SYSTEM, content = systemPrompt),
            MessageDTO(role = MessageDTO.Roles.USER, content = userPrompt)
        )
        val textGenerationPrompt = TextGenerationPromptBody(
            messages = promptMessages,
            model = "gpt-3.5-turbo-0613"
        )
        // request to generate text based on prompt body
        return try {
            // generate text
            val generatedTextResponse = textGeneratorClient.getModelResponseForConversations(
                textGenerationPostBody = textGenerationPrompt
            ).getBodyOrThrowException()
                .generatedResponses
                .first().message
                .content
            // save the generated text in database
            val generatedTextForLocationEntity = GeneratedTextForLocationEntity(
                nameOfLocation = weatherDetails.nameOfLocation,
                temperature = weatherDetails.temperatureRoundedToInt,
                conciseWeatherDescription = weatherDetails.weatherCondition,
                generatedDescription = generatedTextResponse
            )
            generatedTextCacheDatabaseDao.addGeneratedTextForLocation(generatedTextForLocationEntity)
            // return the result
            Result.success(generatedTextResponse)
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            Result.failure(exception)
        }
    }

}