package com.screaming.architecture

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.*
//import org.springframework.data.annotation.Id
//import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.context.annotation.Primary
import java.util.*
import javax.inject.Named
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//"<editor-fold desc=Presentation Layer>"

//Presentation layer - Request Model
class CreateCompanyRequestModel {
    var name: String? = null
    var website: String? = null
    var missionStatement: String? = null
    var logo: String? = null
}

//Presentation layer - Response Models

//Presentation layer - Create company response model
class CreateCompanyResponseModel {
    var id: UUID? = null
    var name: String? = null
    var link: String? = null
    var missionStatement: String? = null
    var logo: String? = null
    var registration: CompanyRegistration? = null
}

//Presentation  layer - Get companies response model
class RetrieveCompaniesResponseModel {
    var id: UUID? = null
    var name: String? = null
    var link: String? = null
    var missionStatement: String? = null
    var logo: String? = null
}

//Presentation layer - Presenters

//Presentation layer - Create Company presenter
interface CreateCompanyPresenter {
    fun present(companyRequest: CreateCompanyRequestModel): CreateCompanyResponseModel
}

@Named
class CreateCompanyPresenterImpl(private val usecase: CreateCompanyUseCase): CreateCompanyPresenter {
    override fun present(companyRequest: CreateCompanyRequestModel): CreateCompanyResponseModel {
        //convert request model to usecase specific form that has meaning
        val company = Company()
        company.name = companyRequest.name
        company.link = companyRequest.website
        company.missionStatement = companyRequest.missionStatement
        company.logo = companyRequest.logo

        //use case returns build domain result
        val result = usecase.execute(company)

        // convert domain result to response model
        val response = CreateCompanyResponseModel()
        response.id = result.id
        response.name = result.name
        response.link = result.link
        response.missionStatement = result.missionStatement
        response.logo = result.logo
        response.registration = result.registration

        return response
    }
}

//Presentation layer - Retrieve Companies presenter
interface RetrieveCompaniesPresenter {
    fun present(): List<RetrieveCompaniesResponseModel>
}

@Named
class RetrieveCompaniesImpl(val usecase: RetrieveActiveCompaniesUseCase): RetrieveCompaniesPresenter {
    override fun present(): List<RetrieveCompaniesResponseModel> {
        val retrievedCompanies = usecase.execute()
        val responseList: MutableList<RetrieveCompaniesResponseModel> = mutableListOf()

        //convert domain result to response model
        for (companyItem: Any in retrievedCompanies) {

            val companyResponseItem = RetrieveCompaniesResponseModel()

            //Need to typ check for mongo
            if (companyItem is Company) {
                val item: Company = companyItem

                companyResponseItem.id = item.id
                companyResponseItem.name = item.name
                companyResponseItem.link = item.link
                companyResponseItem.missionStatement = item.missionStatement
                companyResponseItem.logo = item.logo
            }

            if (companyItem is MongoCompany) {
                val item: MongoCompany = companyItem

                companyResponseItem.id = item.id
                companyResponseItem.name = item.name
                companyResponseItem.link = item.link
                companyResponseItem.missionStatement = item.missionStatement
                companyResponseItem.logo = item.logo
            }


            responseList.add(companyResponseItem)
        }

        return responseList
    }
}

//Presentation layer - Controller
@RestController
class CompanyController(val createCompanyPresenter: CreateCompanyPresenter,
                        val retrieveCompaniesPresenter: RetrieveCompaniesPresenter) {
    @PostMapping
    @ResponseBody
    fun registerCompany(@RequestBody request: CreateCompanyRequestModel): CreateCompanyResponseModel {
        return createCompanyPresenter.present(request)
    }

    @GetMapping
    @ResponseBody
    fun retrieveActiveCompanies(): List<RetrieveCompaniesResponseModel> {
        return retrieveCompaniesPresenter.present()
    }
}
//"</editor-fold desc=Presentation Layer>"

//"<editor-fold desc=Domain Layer>"

//Domain layer - Entities
class Company {
    var id: UUID? = null
    var name: String? = null
    var link: String? = null
    var missionStatement: String? = null
    var logo: String? = null
    var registration: CompanyRegistration? = null
}

class CompanyRegistration(val date: String) {
    var status: RegistrationStatus = RegistrationStatus.ACCEPTED
}

@Suppress("UNUSED")
enum class RegistrationStatus {
    ACCEPTED,
    DECLINED,
    NONE
}
//"</editor-fold desc=Domain Layer>"

//"<editor-fold desc=Application Layer>"

//Application layer - Gateway
interface CompanyGateway {
    fun persist(company: Company)
    fun findAll(): List<Any>
}

// Application layer - Interactors

// Application layer - Interactor: Create Company
interface CreateCompanyUseCase {
    fun execute(request: Company): Company
}

@Named
class CreateCompanyImpl(private val companyGateway: CompanyGateway): CreateCompanyUseCase {
    //Do usecase specific logic
    override fun execute(request: Company): Company {
        val registrationDateString = DateUtility.getStringFromDateTime(LocalDateTime.now())
        request.id = UUID.randomUUID()
        request.registration = CompanyRegistration(registrationDateString)

        //persist
        companyGateway.persist(request)

        return request
    }
}

//Application layer - Interactor: Get Companies
interface RetrieveActiveCompaniesUseCase {
    fun execute(): List<Any>
}

@Named
class RetrieveActiveCompaniesImpl(private val companyGateway: CompanyGateway): RetrieveActiveCompaniesUseCase {
    //Do use case specific logic
    override fun execute(): List<Any> {
        //query
        return companyGateway.findAll()
    }
}
//"</editor-fold desc=Application Layer>"

//"<editor-fold desc=Infrastructure Layer>"

//Infrastructure layer - Persistence

//In-memory implementation
@Primary
@Named
class MapCompanyGateway: CompanyGateway {
    private val dataStore: MutableMap<UUID, Company> = mutableMapOf()

    override fun findAll(): List<Any> = dataStore.values.toList()

    override fun persist(company: Company) {
        dataStore[company.id!!] = company
    }
}

//MongoDB implementation
class MongoCompany {
//    @Id
    var id: UUID? = null
    var name: String? = null
    var link: String? = null
    var missionStatement: String? = null
    var logo: String? = null
    var registration: MongoCompanyRegistration = MongoCompanyRegistration()
}

class MongoCompanyRegistration {
    var status: RegistrationStatus? = null
    var date: String? = null
}

//interface MongoCompanyRepository : MongoRepository<MongoCompany, UUID>

// Currently this gateway is not marked as primary therefore not injected
// Need to setup a dockerfile with instructions to setup mongo
//@Primary
@Named
//class MongoCompanyGateway(private val companyRepository: MongoCompanyRepository): CompanyGateway {
//
//    override fun findAll(): List<Any> {
//        return companyRepository.findAll().toList()
//    }
//
//    override fun persist(company: Company) {
//        val mongoCompany = MongoCompany()
//        mongoCompany.id = company.id
//        mongoCompany.name = company.name
//        mongoCompany.link = company.link
//        mongoCompany.missionStatement = company.missionStatement
//        mongoCompany.logo = company.logo
//        mongoCompany.registration.date = company.registration!!.date
//        mongoCompany.registration.status = company.registration!!.status
//
//        companyRepository.save(mongoCompany)
//    }
//}
//"</editor-fold desc=Infrastructure Layer>"

//"<editor-fold desc=Crosscutting concerns Layer>"

//Util
class DateUtility {
    companion object {
        fun getStringFromDateTime(date: LocalDateTime, format: String? = "yyyy-MM-dd HH:mm:ss"): String {
            val formatter = DateTimeFormatter.ofPattern(format)
            return date.format(formatter)
        }

        @Suppress("UNUSED")
        fun getDateTimeFromString(date: String, format: String? = "yyyy-MM-dd"): LocalDateTime {
            val formatter = DateTimeFormatter.ofPattern(format)
            return LocalDateTime.parse(date, formatter)
        }
    }
}
//"</editor-fold desc=Crosscutting concerns Layer>"

//Main driver
@SpringBootApplication
class MainDriver

fun main(args: Array<String>) {
    SpringApplication.run(MainDriver::class.java, *args)
}