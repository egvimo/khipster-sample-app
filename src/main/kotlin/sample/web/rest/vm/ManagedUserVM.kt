package sample.web.rest.vm

import sample.service.dto.AdminUserDTO

/**
 * View Model extending the [AdminUserDTO], which is meant to be used in the user management UI.
 */
class ManagedUserVM : AdminUserDTO() {

    override fun toString() = "ManagedUserVM{${super.toString()}}"
}
