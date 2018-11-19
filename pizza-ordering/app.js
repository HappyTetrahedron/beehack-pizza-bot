const puppeteer = require('puppeteer');
const Dieci = require('./dieci');

const PERSONAL_DATA = {
    firstName: 'Jimmy',
    lastName: 'Pizzaiolo',
    email: 'jimmy.pizzaiolo@beekeeper.io',
    phone: '+41 78 123 45 67',
    company: 'Beekeeper',
    street: 'Hönggerstrasse',
    postalCode: '8037',
    city: 'Zürich',
    streetNumber: '65',
    department: 'Gameboarding',
};

async function initializePuppeteer() {


    return { browser, page };
}

async function main(ordersJson) {
    try {
        const orders = JSON.parse(ordersJson);

        browser = await puppeteer.launch({ headless: true });
        const page = await browser.newPage();
        await page.setViewport({ width: 2000, height: 1500});

        await Dieci.goToMenuPage(page, { postCode: '8037' });

        await orders.reduce((promiseChain, order) => {
            return promiseChain.then(() => Dieci.addToShoppingCart(page, order));
        }, Promise.resolve());

        await Dieci.goToShoppingCart(page);
        await Dieci.fillPersonalDataForm(page, PERSONAL_DATA);

        await browser.close();
    } catch (err) {
        console.error(err);
        if (browser) {
            await browser.close();
        }
        process.exit(1);
    }
}


// console.log(JSON.stringify(orders));

main(process.argv[2]);