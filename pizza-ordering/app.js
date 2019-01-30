const puppeteer = require('puppeteer');
const Dieci = require('./dieci');

const PERSONAL_DATA = {
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

async function main(orders, contactDetails, creditCard, dryRun) {
    try {
        browser = await puppeteer.launch({ headless: true });
        const page = await browser.newPage();
        await page.setViewport({ width: 2000, height: 1500});

        await Dieci.goToMenuPage(page, { postCode: '8037' });

        await orders.reduce((promiseChain, order) => {
            return promiseChain.then(() => Dieci.addToShoppingCart(page, order));
        }, Promise.resolve());

        await Dieci.goToShoppingCart(page);

        const personalData = Object.assign(PERSONAL_DATA, contactDetails);
        await Dieci.fillPersonalDataForm(page, personalData, creditCard);
        if (dryRun) {
            console.log('Skipping submit (dry run)');
        } else {
            console.log('Submitting order now');
            await Dieci.executeOrder66(page);
        }

        await browser.close();
    } catch (err) {
        console.error(err);
        if (browser) {
            await browser.close();
        }
        process.exit(1);
    }
}


try {
    const orders = JSON.parse(process.argv[2]);
    const contactDetails = JSON.parse(process.argv[3]);
    const creditCard = JSON.parse(process.argv[4]);
    const dryRun = process.argv[5] === '-dry';
    main(orders, contactDetails, creditCard, dryRun);
} catch (error) {
    console.error(err);
    process.exit(1);
}
